# Rapport de Bug — Preprocessing TFLite Android

**Projet** : AgriPredict  
**Date** : 9 mars 2026  
**Composant** : `TFLiteClassifier.kt` — Module de diagnostic IA  
**Sévérité** : Critique — Rendait l'inférence IA inutilisable  
**Statut** : Résolu ✅

---

## 1. Symptôme observé

Lors des tests sur émulateur Android, le modèle de classification des maladies des plantes produisait systématiquement :

- Des **prédictions incorrectes** (classe erronée)
- Des **scores de confiance faibles** (~35–47 %), incompatibles avec un modèle entraîné à ~82 % d'accuracy

Exemple : une image de `Tomato___Late_blight` était classée `Corn_(maize)___healthy` avec 40,6 % de confiance.

## 2. Architecture du modèle

Le modèle MobileNetV2 utilisé intègre une couche de normalisation **à l'intérieur** de son graphe :

```
Input [0, 255] → Rescaling(scale=1/127.5, offset=-1) → [-1, 1] → MobileNetV2 → Dense(24, softmax)
```

Cette couche `Rescaling` est incluse dans le fichier `.keras` exporté ainsi que dans tous les fichiers `.tflite` dérivés (float32, float16, int8). Le modèle attend donc des **pixels bruts en float32 dans l'intervalle [0, 255]**.

De plus, le modèle INT8 quantifié (`mobilenetv2_agri_int8.tflite`) utilise une **quantification interne uniquement** : les poids et activations sont en INT8, mais l'interface externe (tenseurs d'entrée/sortie) reste en **FLOAT32**.

| Propriété | Valeur |
|---|---|
| Format d'entrée | `float32`, shape `[1, 224, 224, 3]` |
| Plage d'entrée attendue | `[0.0, 255.0]` |
| Format de sortie | `float32`, shape `[1, 24]` |
| Quantification | Interne (poids/activations INT8, interface FLOAT32) |

## 3. Cause racine

La méthode `bitmapToByteBufferFloat32()` du classifieur Android appliquait une **normalisation [0, 1]** en divisant chaque composante RGB par 255 :

```kotlin
// Code erroné
byteBuffer.putFloat((pixel shr 16 and 0xFF) / 255.0f)  // R → [0, 1]
byteBuffer.putFloat((pixel shr 8  and 0xFF) / 255.0f)   // G → [0, 1]
byteBuffer.putFloat((pixel        and 0xFF) / 255.0f)   // B → [0, 1]
```

Le modèle recevait donc des valeurs dans `[0, 1]`, que sa couche interne `Rescaling` transformait en :

$$
x_{\text{out}} = x_{\text{in}} \times \frac{1}{127.5} - 1
$$

Pour $x_{\text{in}} \in [0, 1]$ :

$$
x_{\text{out}} \in \left[-1,\; \frac{1}{127.5} - 1\right] \approx [-1.0,\; -0.992]
$$

Toutes les valeurs d'entrée se retrouvaient concentrées autour de $-1$, produisant une **image quasi uniforme** (noire) pour le réseau — d'où les prédictions aléatoires.

## 4. Validation expérimentale

Un script Python de diagnostic a été exécuté pour comparer les deux préprocessings sur les mêmes images :

| Image test | [0, 255] correct | [0, 1] bug Android |
|---|---|---|
| `Tomato___Late_blight` | ✅ Tomato Late Blight — **84,8 %** | ❌ Corn healthy — 40,6 % |
| `Corn___Common_rust` | ✅ Corn Common Rust — **98,4 %** | ✅ Corn Common Rust — 39,1 % |
| `Potato___Early_blight` | ✅ Potato Early Blight — **99,6 %** | ❌ Corn Common Rust — 36,7 % |
| `Pepper___Bacterial_spot` | ✅ Pepper Bacterial Spot — **87,5 %** | ❌ Corn Common Rust — 47,7 % |
| `Cassava___mosaic_disease` | ✅ Cassava Mosaic — **66,4 %** | ❌ Corn Common Rust — 41,4 % |

Le modèle fonctionne correctement avec des pixels bruts `[0, 255]`. Le preprocessing Android était la seule source d'erreur.

## 5. Correction appliquée

Fichier modifié : `mobile/.../util/TFLiteClassifier.kt`, méthode `bitmapToByteBufferFloat32()`.

```kotlin
// Correction : pixels bruts en float32, sans normalisation
byteBuffer.putFloat((pixel shr 16 and 0xFF).toFloat())  // R → [0, 255]
byteBuffer.putFloat((pixel shr 8  and 0xFF).toFloat())   // G → [0, 255]
byteBuffer.putFloat((pixel        and 0xFF).toFloat())   // B → [0, 255]
```

La couche `Rescaling` intégrée au modèle se charge de la conversion `[0, 255] → [-1, 1]`.

## 6. Leçon retenue

> Lorsqu'un modèle TFLite intègre une couche de normalisation dans son graphe, le code d'inférence côté client **ne doit pas appliquer de normalisation supplémentaire**. Il est impératif de vérifier le pipeline de preprocessing du modèle (couches incluses, plage d'entrée attendue, paramètres de quantification) avant d'écrire le code de conversion image → tenseur.

De plus, un modèle quantifié INT8 n'implique pas nécessairement une interface UINT8 : la quantification peut être **interne uniquement**, avec des tenseurs d'entrée/sortie qui restent en FLOAT32.
