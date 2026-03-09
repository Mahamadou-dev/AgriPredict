# 🌾 AgriPredict — Récapitulatif du Projet

> **Application mobile agricole intelligente**
> Projet de fin d'études (PFE) — Licence en Génie Logiciel
> Dernière mise à jour : 8 mars 2026

---

## 📋 Table des matières

1. [Vision du projet](#-vision-du-projet)
2. [Stack technique](#-stack-technique)
3. [Architecture du projet](#-architecture-du-projet)
4. [Ce qui a été fait](#-ce-qui-a-été-fait)
5. [Ce qui reste à faire](#-ce-qui-reste-à-faire)
6. [Prochaine étape](#-prochaine-étape)
7. [Comment tester ce qui a été fait](#-comment-tester-ce-qui-a-été-fait)
8. [Problèmes connus](#-problèmes-connus)
9. [Roadmap visuelle](#-roadmap-visuelle)

---

## 🎯 Vision du projet

**AgriPredict** est une application Android qui aide les agriculteurs d'Afrique de l'Ouest à :

- 📷 **Diagnostiquer les maladies** de leurs plantes en prenant une photo
- 🤖 **Obtenir une analyse IA** via un modèle TensorFlow Lite embarqué (INT8)
- 💊 **Recevoir des traitements** recommandés
- 🔔 **Consulter des alertes** agricoles régionales
- 📚 **Apprendre** sur les maladies des plantes
- 👨‍🌾 **Contacter un expert** agricole

**Principes fondamentaux :**
- 📴 **Offline-first** : fonctionne sans Internet
- 🌍 **Multilingue** : Français, Anglais, Hausa, Zarma
- 🧹 **Simple et pédagogique** : code explicable devant un jury

---

## ⚙️ Stack technique

| Composant | Technologie | Version |
|-----------|------------|---------|
| Langage | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | BOM 2024.12.01 |
| Architecture | Clean Architecture + MVVM | Google recommended |
| Base de données locale | Room (SQLite) | 2.7.1 |
| API REST | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Sérialisation | Kotlinx Serialization | 1.7.3 |
| Coroutines | Kotlinx Coroutines | 1.9.0 |
| Synchronisation | WorkManager | 2.10.0 |
| IA embarquée | TensorFlow Lite | 2.16.1 |
| Images | Coil | 2.7.0 |
| Préférences | DataStore | 1.1.1 |
| Annotation Processing | KSP | 2.0.21-1.0.27 |
| Build | AGP 9.0.1 / Gradle 9.2.1 | — |
| Min SDK | 26 (Android 8.0) | ~90% des appareils |

---

## 🏗️ Architecture du projet

```
app/src/main/java/com/example/agripredict/
│
├── 📦 AgriPredictApplication.kt   ← Point d'entrée Application (init DI)
├── 📦 MainActivity.kt             ← Activité principale (Navigation Compose)
│
├── 📂 data/                        ← COUCHE DONNÉES
│   ├── 📂 local/                   ← Base de données Room
│   │   ├── AgriPredictDatabase.kt  ← Database Room (9 tables)
│   │   ├── Converters.kt          ← TypeConverters (SyncStatus ↔ String)
│   │   ├── 📂 entity/             ← 9 Entities (@Entity Room)
│   │   │   ├── UserEntity.kt           → utilisateur_local
│   │   │   ├── DiagnosticEntity.kt      → diagnostic_local
│   │   │   ├── ImageEntity.kt           → image_local
│   │   │   ├── LocationEntity.kt        → location_local
│   │   │   ├── PredictionEntity.kt      → prediction_local
│   │   │   ├── MaladieEntity.kt         → maladie_local
│   │   │   ├── TraitementEntity.kt      → traitement_local
│   │   │   ├── AlerteEntity.kt          → alerte_local
│   │   │   └── ModeleIAEntity.kt        → modele_ia_local
│   │   └── 📂 dao/                ← 9 DAOs (CRUD complet)
│   │       ├── UserDao.kt
│   │       ├── DiagnosticDao.kt
│   │       ├── ImageDao.kt
│   │       ├── LocationDao.kt
│   │       ├── PredictionDao.kt
│   │       ├── MaladieDao.kt
│   │       ├── TraitementDao.kt
│   │       ├── AlerteDao.kt
│   │       └── ModeleIADao.kt
│   │
│   ├── 📂 remote/                  ← API serveur
│   │   ├── 📂 api/
│   │   │   └── AgriPredictApi.kt   ← Interface Retrofit (endpoints)
│   │   └── 📂 dto/
│   │       ├── 📂 uplink/          ← DTOs envoi → serveur
│   │       │   ├── UserSyncDTO.kt
│   │       │   └── DiagnosticUploadDTO.kt
│   │       └── 📂 downlink/        ← DTOs réception ← serveur
│   │           └── DownlinkDTOs.kt
│   │
│   ├── 📂 repository/              ← Implémentations concrètes
│   │   └── DiagnosticRepositoryImpl.kt
│   │
│   └── 📂 preferences/             ← Préférences utilisateur
│       └── LanguagePreferences.kt
│
├── 📂 domain/                       ← COUCHE MÉTIER
│   ├── 📂 model/                   ← Modèles métier
│   │   ├── DiagnosticResult.kt
│   │   └── Maladie.kt (+ Traitement)
│   ├── 📂 repository/              ← Interfaces (contrats)
│   │   ├── DiagnosticRepository.kt
│   │   └── MaladieRepository.kt
│   └── 📂 usecase/                 ← Cas d'utilisation
│       ├── GetDiagnosticsUseCase.kt
│       └── SaveDiagnosticUseCase.kt
│
├── 📂 ui/                           ← COUCHE INTERFACE
│   ├── 📂 theme/                   ← Thème Material 3 agricole
│   │   ├── Color.kt                ← Palette vert/marron/orange
│   │   ├── Theme.kt                ← Thème clair/sombre
│   │   └── Type.kt                 ← Typographie
│   ├── 📂 navigation/
│   │   ├── Screen.kt               ← Routes (sealed class)
│   │   └── AgriPredictNavGraph.kt  ← Graphe de navigation
│   ├── 📂 screens/
│   │   ├── 📂 home/HomeScreen.kt           ← Grille d'accueil (6 boutons)
│   │   ├── 📂 diagnostic/DiagnosticScreen.kt ← Écran diagnostic IA
│   │   ├── 📂 diseases/DiseasesScreen.kt    ← Base de connaissances
│   │   ├── 📂 alerts/AlertsScreen.kt       ← Alertes agricoles
│   │   ├── 📂 history/HistoryScreen.kt     ← Historique diagnostics
│   │   ├── 📂 expert/ExpertScreen.kt       ← Contact expert
│   │   ├── 📂 about/AboutScreen.kt         ← À propos
│   │   └── 📂 settings/SettingsScreen.kt   ← Paramètres (langue)
│   └── 📂 components/              ← (vide — prêt pour composants réutilisables)
│
├── 📂 sync/                         ← SYNCHRONISATION OFFLINE-FIRST
│   ├── SyncStatus.kt               ← enum PENDING / SYNCED / FAILED
│   ├── SyncManager.kt              ← Coordinateur de sync
│   ├── SyncWorker.kt               ← WorkManager background
│   ├── SyncRepository.kt           ← Interface sync
│   └── NetworkChecker.kt           ← Détection connectivité
│
├── 📂 di/                           ← INJECTION DE DÉPENDANCES
│   └── AppContainer.kt             ← Conteneur DI manuel (simple)
│
└── 📂 util/                         ← UTILITAIRES
    └── LocaleManager.kt            ← Changement de langue dynamique

res/                                  ← RESSOURCES i18n
├── values/strings.xml               ← 🇫🇷 Français (par défaut)
├── values-en/strings.xml            ← 🇬🇧 English
├── values-ha/strings.xml            ← 🇳🇬 Hausa
├── values-dje/strings.xml           ← 🇳🇪 Zarma
└── xml/locales_config.xml           ← Config Per-App Language (Android 13+)
```

---

## ✅ Ce qui a été fait

### Phase 1 — Initialisation du projet ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| Projet Android Kotlin | ✅ | Empty Compose Activity, minSdk 26 |
| build.gradle.kts | ✅ | 18 dépendances configurées |
| libs.versions.toml | ✅ | Catalogue de versions centralisé |
| Plugins | ✅ | AGP, Compose, Serialization, KSP |
| AndroidManifest.xml | ✅ | Permissions caméra, réseau, stockage, GPS, RTL |
| Compilation | ✅ | `BUILD SUCCESSFUL` confirmé |

### Phase 2 — Architecture Clean Architecture ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| Structure `data/` | ✅ | local, remote, repository, preferences |
| Structure `domain/` | ✅ | model, repository, usecase |
| Structure `ui/` | ✅ | navigation, screens (8), components, theme |
| Structure `sync/` | ✅ | SyncStatus, SyncManager, SyncWorker |
| Structure `di/` | ✅ | AppContainer (DI manuelle simple) |
| Structure `util/` | ✅ | LocaleManager |

### Phase 3 — Base de données locale (Room) ✅
| Table | Entity | DAO | ForeignKeys | SyncStatus |
|-------|--------|-----|-------------|------------|
| `utilisateur_local` | ✅ UserEntity | ✅ UserDao | — | — |
| `diagnostic_local` | ✅ DiagnosticEntity | ✅ DiagnosticDao | FK→User | ✅ PENDING/SYNCED/FAILED |
| `image_local` | ✅ ImageEntity | ✅ ImageDao | FK→Diagnostic | — |
| `location_local` | ✅ LocationEntity | ✅ LocationDao | — | — |
| `prediction_local` | ✅ PredictionEntity | ✅ PredictionDao | FK→Diagnostic, FK→Maladie | — |
| `maladie_local` | ✅ MaladieEntity | ✅ MaladieDao | — | — |
| `traitement_local` | ✅ TraitementEntity | ✅ TraitementDao | FK→Maladie | — |
| `alerte_local` | ✅ AlerteEntity | ✅ AlerteDao | FK→Maladie | ✅ PENDING/SYNCED/FAILED |
| `modele_ia_local` | ✅ ModeleIAEntity | ✅ ModeleIADao | — | — |

**Chaque DAO** possède : `insert`, `update`, `delete`, `getById`, `getAll`, `observeAll() → Flow<List<>>`

### Phase 3 bis — DTOs de synchronisation ✅
| Direction | DTO | Statut |
|-----------|-----|--------|
| UPLINK ↑ | UserSyncDTO | ✅ |
| UPLINK ↑ | DiagnosticUploadDTO + LocationUploadDTO + PredictionUploadDTO | ✅ |
| UPLINK ↑ | DiagnosticUploadResponseDTO | ✅ |
| DOWNLINK ↓ | CheckUpdatesResponseDTO | ✅ |
| DOWNLINK ↓ | KnowledgeBaseDTO (MaladieDTO + TraitementDTO) | ✅ |
| DOWNLINK ↓ | AlerteDTO | ✅ |
| DOWNLINK ↓ | ModelUpdateDTO | ✅ |
| DOWNLINK ↓ | AppConfigDTO | ✅ |
| DOWNLINK ↓ | UpdateBundleDTO | ✅ |

### Phase 3 ter — Synchronisation offline-first (squelette) ✅
| Composant | Statut | Détail |
|-----------|--------|--------|
| SyncStatus | ✅ | enum PENDING / SYNCED / FAILED |
| SyncType | ✅ | enum UPLINK / DOWNLINK |
| SyncManager | ✅ | Squelette avec TODO (syncUserProfile, syncDiagnostics, checkUpdates) |
| SyncWorker | ✅ | WorkManager CoroutineWorker squelette |
| SyncRepository | ✅ | Interface sync |
| NetworkChecker | ✅ | Vérification ConnectivityManager |

### Phase 4 — UI Écran d'accueil ✅
| Composant | Statut | Détail |
|-----------|--------|--------|
| HomeScreen | ✅ | Grille 2×3 avec 6 boutons (icônes Material) |
| Navigation Compose | ✅ | 8 destinations (Home, Diagnostic, Diseases, Alerts, History, Expert, About, Settings) |
| Thème agricole | ✅ | Vert/marron/orange, clair/sombre |
| Écrans placeholder | ✅ | 7 écrans avec TopAppBar et bouton retour |
| SettingsScreen | ✅ | Sélecteur de langue fonctionnel (4 langues) |

### Internationalisation (i18n) ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| strings.xml (FR) | ✅ | 68 lignes — langue par défaut |
| strings.xml (EN) | ✅ | 54 lignes — traduction anglaise |
| strings.xml (HA) | ✅ | 54 lignes — traduction hausa |
| strings.xml (DJE) | ✅ | 54 lignes — traduction zarma |
| locales_config.xml | ✅ | Per-App Language (Android 13+) |
| LocaleManager.kt | ✅ | Changement de langue dynamique |
| LanguagePreferences.kt | ✅ | DataStore persistant |
| Aucun texte en dur | ✅ | 100% via `stringResource(R.string.xxx)` |
| android:supportsRtl | ✅ | Activé dans le Manifest |

---

## 🔲 Ce qui reste à faire

### Phase 5 — Diagnostic IA (PRIORITÉ N°1) 🔲
- [ ] Intégrer la caméra (CameraX ou ActivityResultContract)
- [ ] Charger un modèle TensorFlow Lite INT8
- [ ] Créer `TFLiteClassifier.kt` dans `util/`
- [ ] Créer `DiagnosticViewModel.kt`
- [ ] Connecter UI → ViewModel → UseCase → Repository → IA Engine
- [ ] Afficher résultat (maladie, confiance %, traitement)
- [ ] Sauvegarder le diagnostic dans Room

### Phase 6 — Authentification 🔲
- [ ] Écran d'inscription (nom, téléphone, commune, village)
- [ ] Écran de connexion
- [ ] Écran de profil
- [ ] Sauvegarde locale du profil (UserDao)
- [ ] Session utilisateur (DataStore)

### Phase 7 — Écrans fonctionnels 🔲
- [ ] Historique des diagnostics (lire depuis Room)
- [ ] Base de connaissances maladies (lire depuis Room)
- [ ] Alertes agricoles (lire depuis Room)
- [ ] Contact expert (formulaire)
- [ ] À propos (contenu dynamique)

### Phase 8 — Synchronisation complète 🔲
- [ ] Implémenter `SyncManager` (remplacer les TODO)
- [ ] Implémenter `SyncWorker` avec WorkManager
- [ ] Configurer la sync périodique en arrière-plan
- [ ] Gérer les conflits de données
- [ ] Implémenter le retry pour les FAILED

### Phase 9 — Backend API 🔲
- [ ] Configurer l'URL de base Retrofit
- [ ] Implémenter les appels API réels
- [ ] Gérer l'authentification API (token)
- [ ] Tests de connexion

### Phase 10 — Finitions 🔲
- [ ] Gestion des permissions runtime (caméra, GPS)
- [ ] Gestion des erreurs (écrans d'erreur)
- [ ] Écrans de chargement (loading states)
- [ ] Tests unitaires
- [ ] Tests d'instrumentation
- [ ] Optimisation performance
- [ ] Logo et assets finaux

---

## 🎯 Prochaine étape immédiate

### → Phase 6 : Authentification

C'est la prochaine fonctionnalité logique. Plan détaillé :

```
1. Écran d'inscription
   └── ui/screens/auth/RegisterScreen.kt
       → Champs : nom, téléphone, commune, village
       → Bouton "S'inscrire"
       → Sauvegarde dans UserDao (Room)

2. Écran de connexion
   └── ui/screens/auth/LoginScreen.kt
       → Champ : téléphone
       → Bouton "Se connecter"
       → Vérification locale (Room)

3. Gestion de session
   └── data/preferences/SessionPreferences.kt
       → DataStore : userId, isLoggedIn
       → Persiste entre les lancements de l'app

4. Écran profil
   └── ui/screens/auth/ProfileScreen.kt
       → Affiche les infos de l'utilisateur
       → Bouton "Déconnexion"

5. Protection des écrans
   └── Navigation conditionnelle
       → Si pas connecté → LoginScreen
       → Si connecté → HomeScreen

6. Connecter le diagnostic à l'utilisateur réel
   └── Remplacer "local_user" par le vrai userId
```

### 🔌 Quand commencer le backend ?

Le backend sera nécessaire à partir de la **Phase 8 (Synchronisation complète)**.
Cependant, il est recommandé de **commencer son développement en parallèle de la Phase 7**,
car à ce moment-là :
- ✅ Les DTOs sont déjà définis côté mobile (uplink + downlink)
- ✅ L'interface Retrofit (AgriPredictApi.kt) existe déjà
- ✅ Les modèles de données sont stables
- 🎯 Tu pourras tester la synchronisation réelle dès la Phase 8

**Résumé :** Développe le backend pendant les Phases 7-8. Les endpoints requis
correspondent directement aux DTOs créés dans `data/remote/dto/`.

---

## 🧪 Comment tester ce qui a été fait

### Test 1 — Compilation ✅
```powershell
cd C:\GREMAHTECH\GremahTech\PFE\AgriPredict\mobile
.\gradlew.bat assembleDebug
```
**Résultat attendu :** `BUILD SUCCESSFUL`

### Test 2 — Lancer l'application sur émulateur/appareil
1. Ouvrir Android Studio
2. **File → Sync Project with Gradle Files** (résout les erreurs rouges IDE)
3. Sélectionner un émulateur API 26+ ou un appareil physique
4. Cliquer sur ▶️ Run

**Vérifier :**
- [ ] L'écran d'accueil s'affiche avec les 6 boutons
- [ ] Le titre "AgriPredict" apparaît dans la TopAppBar verte
- [ ] Chaque bouton navigue vers le bon écran
- [ ] Le bouton retour (←) fonctionne sur chaque écran
- [ ] L'icône ⚙️ ouvre l'écran Paramètres

### Test 3 — Navigation complète
Tester ce parcours :
```
Accueil → Diagnostic → ← Retour
Accueil → Maladies → ← Retour
Accueil → Alertes → ← Retour
Accueil → Historique → ← Retour
Accueil → Expert → ← Retour
Accueil → À propos → ← Retour
Accueil → ⚙️ Paramètres → ← Retour
```

### Test 4 — Changement de langue
1. Accueil → ⚙️ Paramètres
2. Sélectionner "English"
3. Vérifier que les textes changent en anglais
4. Sélectionner "Hausa"
5. Vérifier que les textes changent en hausa
6. Revenir à "Français"

### Test 5 — Thème sombre
1. Activer le mode sombre dans les paramètres Android de l'émulateur
2. Relancer l'app
3. Vérifier que le thème sombre s'applique (couleurs adaptées)

### Test 6 — Diagnostic IA (mode démo)
1. Accueil → "Faire un diagnostic"
2. Accepter la permission caméra
3. Appuyer sur "Prendre une photo" → la caméra s'ouvre
4. Prendre une photo → l'aperçu s'affiche
5. Appuyer sur "Analyser" → l'animation de chargement apparaît
6. Le résultat s'affiche avec :
   - Badge "⚠ Mode démonstration" (pas de modèle .tflite)
   - Un nom de maladie aléatoire
   - Une barre de confiance avec pourcentage
7. Appuyer sur "Enregistrer" → message de succès
8. Appuyer sur "Nouveau diagnostic" → retour à l'état initial

### Test 7 — Base de données Room (test unitaire)
Créer un test simple dans `app/src/androidTest/` :
```kotlin
// Vérifier que la base de données se crée sans erreur
// Vérifier qu'on peut insérer et lire un UserEntity
// Vérifier qu'on peut insérer et lire un DiagnosticEntity
```
*(Ce test sera créé à la prochaine étape)*

### Test 8 — APK généré
```powershell
# Vérifier que l'APK a bien été généré
Test-Path "app\build\outputs\apk\debug\app-debug.apk"
```

---

## ⚠️ Problèmes connus

### 1. Erreurs rouges dans l'IDE (faux positifs)
**Symptôme :** Des erreurs `Unresolved reference` apparaissent en rouge dans l'éditeur
**Cause :** L'IDE n'a pas synchronisé Gradle après l'ajout des dépendances
**Solution :** `File → Sync Project with Gradle Files` dans Android Studio
**Impact :** Aucun — le build Gradle compile parfaitement

### 2. TensorFlow Lite Support désactivé temporairement
**Symptôme :** `tensorflow-lite-support` est commenté dans build.gradle.kts
**Cause :** Conflit de namespace avec AGP 9.0.1 (bug Google)
**Solution :** Sera réactivé quand Google publiera un fix ou quand on passera à une version compatible
**Impact :** N'empêche pas le développement du diagnostic IA (on utilise `tensorflow-lite` directement)

### 3. Warning expérimental KSP
**Symptôme :** `WARNING: The option setting 'android.disallowKotlinSourceSets=false' is experimental`
**Cause :** KSP avec le Kotlin intégré d'AGP 9 nécessite ce flag
**Solution :** Flag dans gradle.properties — sera retiré quand KSP sera officiellement compatible
**Impact :** Aucun — le build fonctionne correctement

---

## 📊 Roadmap visuelle

```
Étape                          Statut    Priorité
──────────────────────────────────────────────────
1. Setup projet + Gradle       ✅ FAIT    —
2. Architecture Clean Arch     ✅ FAIT    —
3. Base de données Room        ✅ FAIT    —
4. DTOs synchronisation        ✅ FAIT    —
5. Sync offline-first (skel)   ✅ FAIT    —
6. UI Home + Navigation        ✅ FAIT    —
7. Thème Material 3 agricole   ✅ FAIT    —
8. i18n (4 langues)            ✅ FAIT    —
──────────────────────────────────────────────────
9.  🎯 DIAGNOSTIC IA           🔲 NEXT   ★★★★★
10. Authentification            🔲        ★★★★
11. Historique diagnostics      🔲        ★★★
12. Base connaissances          🔲        ★★★
13. Alertes agricoles           🔲        ★★
14. Sync complète               🔲        ★★
15. Contact expert              🔲        ★
16. Tests                       🔲        ★★★
17. Finitions + polish          🔲        ★★
──────────────────────────────────────────────────
```

---

## 📁 Fichiers clés à connaître

| Fichier | Rôle |
|---------|------|
| `app/build.gradle.kts` | Dépendances et configuration du module |
| `gradle/libs.versions.toml` | Catalogue centralisé de toutes les versions |
| `gradle.properties` | Flags Gradle (KSP, AndroidX) |
| `AndroidManifest.xml` | Permissions, Application class, locale config |
| `AgriPredictApplication.kt` | Initialise le conteneur DI au démarrage |
| `AppContainer.kt` | Fournit toutes les dépendances (Database, DAOs, Repos, IA, ViewModels) |
| `AgriPredictDatabase.kt` | Définit les 9 tables Room |
| `AgriPredictNavGraph.kt` | Graphe de navigation (8 écrans) + injection ViewModel |
| `HomeScreen.kt` | Écran d'accueil principal |
| `DiagnosticScreen.kt` | Écran diagnostic IA complet (caméra → analyse → résultat) |
| `DiagnosticViewModel.kt` | ViewModel MVVM (6 états, analyse IA, sauvegarde) |
| `TFLiteClassifier.kt` | Moteur IA TensorFlow Lite + mode démo |
| `DiagnosticRepositoryImpl.kt` | Repository enrichi (3 DAOs : diagnostic, image, prediction) |
| `SyncStatus.kt` | Enum PENDING/SYNCED/FAILED |
| `LocaleManager.kt` | Changement de langue dynamique |

---

## 👨‍🎓 Notes pour la soutenance

- **Architecture** : Clean Architecture recommandée par Google — séparation data/domain/ui
- **MVVM** : Pattern Model-View-ViewModel avec Flow/StateFlow
- **Offline-first** : Les données sont d'abord stockées localement puis synchronisées
- **i18n** : 4 langues supportées sans modifier le code source
- **DI manuelle** : Choix pédagogique — plus simple que Hilt/Dagger à expliquer
- **SyncStatus** : Chaque enregistrement sait s'il est PENDING, SYNCED ou FAILED
- **TypeConverters** : Room ne stocke pas les enums directement → conversion String

---

> 📌 **Prochain objectif :** Implémenter l'authentification (Phase 6)
> Écrans inscription, connexion, profil + session DataStore + navigation conditionnelle

