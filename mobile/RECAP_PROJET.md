# 🌾 AgriPredict — Récapitulatif du Projet

> **Application mobile agricole intelligente**
> Projet de fin d'études (PFE) — Licence en Génie Logiciel
> Dernière mise à jour : 10 mars 2026 — **Audit de stabilité effectué** ⚡

---

## 📋 Table des matières

1. [Vision du projet](#-vision-du-projet)
2. [Stack technique](#-stack-technique)
3. [Architecture du projet](#-architecture-du-projet)
4. [Ce qui a été fait](#-ce-qui-a-été-fait)
5. [Bugs corrigés (audit du 10 mars 2026)](#-bugs-corrigés-audit-du-10-mars-2026)
6. [Ce qui reste à faire](#-ce-qui-reste-à-faire)
7. [Prochaine étape](#-prochaine-étape)
8. [Comment tester ce qui a été fait](#-comment-tester-ce-qui-a-été-fait)
9. [Problèmes connus](#-problèmes-connus)
10. [Santé du projet](#-santé-du-projet)
11. [Roadmap visuelle](#-roadmap-visuelle)

---

## 🎯 Vision du projet

**AgriPredict** est une application Android qui aide les agriculteurs d'Afrique de l'Ouest à :

- 📷 **Diagnostiquer les maladies** de leurs plantes en prenant une photo
- 🤖 **Obtenir une analyse IA** via un modèle TensorFlow Lite embarqué (INT8)
- 💊 **Recevoir des traitements** recommandés
- 🔔 **Consulter des alertes** agricoles régionales
- 📚 **Apprendre** sur les maladies des plantes
- 👨‍🌾 **Contacter un expert** agricole
- 🔐 **S'inscrire et se connecter** avec un profil agriculteur

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
│   │   │   ├── UserEntity.kt
│   │   │   ├── DiagnosticEntity.kt
│   │   │   ├── ImageEntity.kt
│   │   │   ├── LocationEntity.kt
│   │   │   ├── PredictionEntity.kt
│   │   │   ├── MaladieEntity.kt
│   │   │   ├── TraitementEntity.kt
│   │   │   ├── AlerteEntity.kt
│   │   │   └── ModeleIAEntity.kt
│   │   └── 📂 dao/                ← 9 DAOs (CRUD complet)
│   │       ├── UserDao.kt          ← + getByTelephone() + countByTelephone()
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
│   │   └── DiagnosticRepositoryImpl.kt  ← ⚡ Corrigé : transaction Room + ordre FK
│   │
│   └── 📂 preferences/             ← Préférences utilisateur
│       ├── LanguagePreferences.kt   ← DataStore langue (agripredict_preferences)
│       └── SessionPreferences.kt    ← DataStore session auth (session_preferences)
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
│   │   ├── Screen.kt               ← 12 routes (sealed class + HistoryDetail)
│   │   └── AgriPredictNavGraph.kt  ← Graphe de navigation + auth conditionnelle
│   ├── 📂 screens/
│   │   ├── 📂 home/HomeScreen.kt           ← Grille d'accueil (6 boutons + profil)
│   │   ├── 📂 diagnostic/
│   │   │   ├── DiagnosticScreen.kt         ← Écran diagnostic IA (caméra + galerie)
│   │   │   └── DiagnosticViewModel.kt      ← ViewModel (6 états) ⚡ Corrigé : userId vérifié
│   │   ├── 📂 auth/
│   │   │   ├── AuthViewModel.kt            ← ViewModel auth ⚡ Corrigé : try-catch robuste
│   │   │   ├── LoginScreen.kt              ← Écran connexion (téléphone + mot de passe)
│   │   │   ├── RegisterScreen.kt           ← Écran inscription agriculteur
│   │   │   └── ProfileScreen.kt            ← Écran profil + édition + changer MDP + déconnexion
│   │   ├── 📂 diseases/DiseasesScreen.kt    ← Base de connaissances (placeholder)
│   │   ├── 📂 alerts/AlertsScreen.kt       ← Alertes agricoles (placeholder)
│   │   ├── 📂 history/
│   │   │   ├── HistoryScreen.kt            ← Historique diagnostics (placeholder UI)
│   │   │   └── HistoryViewModel.kt         ← ViewModel ⚡ Corrigé : try-catch + Error state
│   │   ├── 📂 expert/ExpertScreen.kt       ← Contact expert (placeholder)
│   │   ├── 📂 about/AboutScreen.kt         ← À propos (placeholder)
│   │   └── 📂 settings/SettingsScreen.kt   ← Paramètres (langue)
│   └── 📂 components/              ← (vide — prêt pour composants réutilisables)
│
├── 📂 sync/                         ← SYNCHRONISATION OFFLINE-FIRST
│   ├── SyncStatus.kt               ← enum PENDING / SYNCED / FAILED
│   ├── SyncManager.kt              ← Coordinateur de sync (squelette)
│   ├── SyncWorker.kt               ← WorkManager background (squelette)
│   ├── SyncRepository.kt           ← Interface sync
│   └── NetworkChecker.kt           ← Détection connectivité
│
├── 📂 di/                           ← INJECTION DE DÉPENDANCES
│   └── AppContainer.kt             ← ⚡ Corrigé : +database dans repo, +historyViewModelFactory
│
└── 📂 util/                         ← UTILITAIRES
    ├── LocaleManager.kt            ← Changement de langue dynamique
    ├── TFLiteClassifier.kt         ← Moteur IA TFLite (pixels bruts [0,255]) + mode démo
    └── LabelFormatter.kt           ← Formatage des labels IA bruts

res/                                  ← RESSOURCES i18n
├── values/strings.xml               ← 🇫🇷 Français (107 lignes — auth + profil inclus)
├── values-en/strings.xml            ← 🇬🇧 English (88 lignes)
├── values-ha/strings.xml            ← 🇳🇬 Hausa (87 lignes)
├── values-dje/strings.xml           ← 🇳🇪 Zarma (87 lignes)
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
| Structure `ui/` | ✅ | navigation, screens (9 dossiers), components, theme |
| Structure `sync/` | ✅ | SyncStatus, SyncManager, SyncWorker |
| Structure `di/` | ✅ | AppContainer (DI manuelle simple) |
| Structure `util/` | ✅ | LocaleManager, TFLiteClassifier, LabelFormatter |

### Phase 3 — Base de données locale (Room) ✅
| Table | Entity | DAO | ForeignKeys | SyncStatus |
|-------|--------|-----|-------------|------------|
| `utilisateur_local` | ✅ UserEntity | ✅ UserDao (+getByTelephone, +countByTelephone) | — | — |
| `diagnostic_local` | ✅ DiagnosticEntity | ✅ DiagnosticDao | FK→User (CASCADE) | ✅ |
| `image_local` | ✅ ImageEntity | ✅ ImageDao | FK→Diagnostic (CASCADE) | — |
| `location_local` | ✅ LocationEntity | ✅ LocationDao | — | — |
| `prediction_local` | ✅ PredictionEntity | ✅ PredictionDao | FK→Diagnostic (CASCADE), FK→Maladie (SET_NULL) | — |
| `maladie_local` | ✅ MaladieEntity | ✅ MaladieDao | — | — |
| `traitement_local` | ✅ TraitementEntity | ✅ TraitementDao | FK→Maladie (CASCADE) | — |
| `alerte_local` | ✅ AlerteEntity | ✅ AlerteDao | FK→Maladie (SET_NULL) | ✅ |
| `modele_ia_local` | ✅ ModeleIAEntity | ✅ ModeleIADao | — | — |

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
| SyncManager | ✅ | Squelette avec TODO |
| SyncWorker | ✅ | WorkManager CoroutineWorker squelette |
| SyncRepository | ✅ | Interface sync |
| NetworkChecker | ✅ | Vérification ConnectivityManager |

### Phase 4 — UI Écran d'accueil ✅
| Composant | Statut | Détail |
|-----------|--------|--------|
| HomeScreen | ✅ | Grille 2×3 (6 boutons) + bouton profil + bienvenue personnalisée |
| Navigation Compose | ✅ | 11 destinations + auth conditionnelle |
| Thème agricole | ✅ | Vert/marron/orange, clair/sombre |
| Écrans placeholder | ✅ | 5 écrans avec TopAppBar et bouton retour |
| SettingsScreen | ✅ | Sélecteur de langue fonctionnel (4 langues) |

### Internationalisation (i18n) ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| strings.xml (FR) | ✅ | ~107 lignes — inclut auth + profil |
| strings.xml (EN) | ✅ | ~88 lignes — traduction anglaise complète |
| strings.xml (HA) | ✅ | ~87 lignes — traduction hausa complète |
| strings.xml (DJE) | ✅ | ~87 lignes — traduction zarma complète |
| locales_config.xml | ✅ | Per-App Language (Android 13+) |
| LocaleManager.kt | ✅ | Changement de langue dynamique |
| LanguagePreferences.kt | ✅ | DataStore persistant |
| Aucun texte en dur | ✅ | 100% via `stringResource(R.string.xxx)` |
| android:supportsRtl | ✅ | Activé dans le Manifest |

### Phase 5 — Diagnostic IA ✅ (⚡ corrigé)
| Composant | Statut | Détail |
|-----------|--------|--------|
| TFLiteClassifier.kt | ✅ | Moteur IA TFLite — pixels bruts [0,255] float32 + mode démo |
| LabelFormatter.kt | ✅ | Parse les labels bruts ("Tomato___Early_blight" → plante + maladie) |
| DiagnosticScreen.kt | ✅ | UI complète : caméra + galerie + aperçu + résultat |
| DiagnosticViewModel.kt | ✅⚡ | 6 états — **corrigé** : vérifie userId non-vide avant sauvegarde |
| Caméra | ✅ | ActivityResultContract (TakePicturePreview) |
| Galerie | ✅ | ActivityResultContract (PickVisualMedia) — choix d'image |
| Analyse IA | ✅ | classify(bitmap) → label + confidence |
| Sauvegarde Room | ✅⚡ | **Corrigé** : transaction Room atomique + ordre FK correct |
| DiagnosticRepositoryImpl | ✅⚡ | **Corrigé** : insert Diagnostic→Image→Prediction (pas l'inverse) |
| SaveDiagnosticUseCase | ✅ | UseCase pour sauvegarder un diagnostic |
| GetDiagnosticsUseCase | ✅ | UseCase pour lister les diagnostics |
| userId connecté | ✅⚡ | **Corrigé** : refuse sauvegarde si userId vide (plus de "local_user") |
| Mode démo | ✅ | Si pas de modèle .tflite → résultat aléatoire avec badge d'avertissement |
| Modèle INT8 déployé | ✅ | `plant_disease_model.tflite` (2.87 Mo, 24 classes, MobileNetV2) |
| Labels déployés | ✅ | `labels.txt` (24 classes réelles depuis class_names.json) |
| Bug preprocessing corrigé | ✅ | Pixels bruts [0,255] float32 — couche Rescaling interne au modèle |

### Phase 6 — Authentification ✅ (⚡ corrigé)
| Composant | Statut | Détail |
|-----------|--------|--------|
| SessionPreferences.kt | ✅ | DataStore séparé (session_preferences) : userId, userName, isLoggedIn |
| AuthViewModel.kt | ✅⚡ | **Corrigé** : try-catch dans toutes les fonctions (checkSession, register, login, loadProfile, updateProfile, changePassword) |
| AuthUiState | ✅ | Sealed class : Loading, LoggedOut, LoggedIn, Error |
| ProfileUpdateState | ✅ | Sealed class : Idle, Loading, Success, PasswordChanged, Error |
| LoginScreen.kt | ✅ | Connexion par téléphone + mot de passe + validation |
| RegisterScreen.kt | ✅ | Inscription (nom, téléphone, commune, village, mot de passe) |
| ProfileScreen.kt | ✅ | Profil + édition infos + changement MDP + déconnexion |
| UserDao enrichi | ✅ | +getByTelephone() +countByTelephone() pour l'auth |
| Navigation conditionnelle | ✅ | Si pas connecté → Login, si connecté → Home |
| AppContainer mis à jour | ✅⚡ | +sessionPreferences, +authViewModelFactory, **+historyViewModelFactory** |
| HomeScreen amélioré | ✅ | +bouton profil (icône personne), +bienvenue personnalisée |
| Screen.kt enrichi | ✅ | +Login, +Register, +Profile, +HistoryDetail (12 routes au total) |
| AgriPredictNavGraph | ✅ | Navigation auth conditionnelle + popUpTo pour empêcher retour |
| DiagnosticViewModel lié | ✅⚡ | **Corrigé** : vérifie userId avant save |
| i18n auth complet | ✅ | 22 nouvelles clés dans les 4 langues (FR, EN, HA, DJE) |
| Icônes AutoMirrored | ✅ | Login + Logout utilisent les versions non-dépréciées |

### Phase 6 bis — HistoryViewModel ✅ (⚡ corrigé)
| Composant | Statut | Détail |
|-----------|--------|--------|
| HistoryViewModel.kt | ✅⚡ | **Corrigé** : try-catch dans loadDiagnostics, loadById, delete |
| HistoryUiState | ✅⚡ | Sealed class : Loading, Success, Empty, **+Error** (ajouté) |
| Route HistoryDetail | ✅ | Navigation dynamique `history_detail/{diagnosticId}` |
| AppContainer enrichi | ✅⚡ | **+historyViewModelFactory** (manquait, ajouté) |

### Phase 6 ter — Audit de stabilité ✅ ← NOUVEAU
| Composant | Statut | Détail |
|-----------|--------|--------|
| Correction FK ordre d'insertion | ✅ | Diagnostic inséré AVANT Image et Prediction |
| Transaction Room atomique | ✅ | `database.withTransaction { }` dans saveDiagnostic |
| Vérification userId avant save | ✅ | Plus de fallback "local_user" invalide |
| try-catch AuthViewModel | ✅ | 6 fonctions protégées contre les crashs |
| try-catch HistoryViewModel | ✅ | 3 fonctions protégées contre les crashs |
| try-catch DiagnosticViewModel | ✅ | saveDiagnostic protégé avec message d'erreur |
| HistoryUiState.Error ajouté | ✅ | Les erreurs de chargement sont communiquées à l'UI |
| historyViewModelFactory | ✅ | Ajouté dans AppContainer (manquait) |
| Build validé | ✅ | `BUILD SUCCESSFUL` confirmé après toutes les corrections |

---

## 🔧 Bugs corrigés (audit du 10 mars 2026)

### 🔴 BUG CRITIQUE 1 : Foreign Key constraint failed — Sauvegarde diagnostic
**Symptôme :** Crash `FOREIGN KEY constraint failed` lors de "Enregistrer" sur l'écran diagnostic
**Cause racine :** `ImageEntity` et `PredictionEntity` (FK → DiagnosticEntity) insérées AVANT le parent `DiagnosticEntity`
**Correction :** Réordonnancement : Diagnostic→Image→Prediction + `database.withTransaction { }`
**Fichiers :** `DiagnosticRepositoryImpl.kt`, `AppContainer.kt`

### 🔴 BUG CRITIQUE 2 : userId "local_user" viole la FK
**Symptôme :** Si l'utilisateur n'est pas connecté, fallback `"local_user"` → FK violation
**Correction :** Vérifie userId non-vide, affiche erreur "Veuillez vous connecter"
**Fichier :** `DiagnosticViewModel.kt`

### 🟡 BUG 3 : Crash au démarrage si DB corrompue
**Symptôme :** Exception dans `checkSession()` → crash écran blanc
**Correction :** try-catch avec fallback vers `AuthUiState.LoggedOut`
**Fichier :** `AuthViewModel.kt`

### 🟡 BUG 4 : Fonctions ViewModel sans gestion d'erreur
**Symptôme :** Toute erreur DB/réseau causait un crash immédiat
**Correction :** try-catch dans 12 fonctions coroutines des 3 ViewModels
**Fichiers :** `AuthViewModel.kt`, `HistoryViewModel.kt`, `DiagnosticViewModel.kt`

### 🟡 BUG 5 : historyViewModelFactory absent de AppContainer
**Symptôme :** Le RECAP affirmait son existence mais il manquait
**Correction :** Factory ajouté dans AppContainer
**Fichier :** `AppContainer.kt`

---

## 🔲 Ce qui reste à faire

### Phase 7 — Écrans fonctionnels 🔲
- [ ] **Historique des diagnostics — UI** (HistoryScreen avec LazyColumn, HistoryDetailScreen)
- [ ] **Base de connaissances maladies** (lire MaladieDao + TraitementDao, afficher fiches)
- [ ] **Alertes agricoles** (lire AlerteDao, afficher par zone + gravité)
- [ ] **Contact expert** (formulaire simple ou numéro de téléphone)
- [ ] **À propos** (informations sur l'app, version, auteur)

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
- [ ] Tests de connexion mobile ↔ serveur

### Phase 10 — Finitions 🔲
- [ ] Gestion des permissions runtime (caméra, GPS)
- [ ] Écrans de chargement (loading states)
- [ ] Tests unitaires
- [ ] Tests d'instrumentation
- [ ] Optimisation performance
- [ ] Logo et assets finaux
- [ ] Remplacer `fallbackToDestructiveMigration` par des migrations Room propres

---

## 🎯 Prochaine étape immédiate

### → Phase 7 : Rendre les écrans fonctionnels

Le `HistoryViewModel` est déjà prêt — il charge, filtre et supprime les diagnostics.

```
Ordre recommandé :

1. Historique des diagnostics — UI (★★★★★)
   └── ui/screens/history/HistoryScreen.kt
       → Le ViewModel existe déjà (charge, filtre, supprime)
       → Remplir : LazyColumn + date, label, confiance %, image miniature
       → HistoryDetailScreen pour le détail d'un diagnostic

2. Base de connaissances maladies (★★★★)
   └── ui/screens/diseases/DiseasesScreen.kt
       → Lire MaladieDao.observeAll() + TraitementDao
       → Prépopuler la base avec des données locales

3. Alertes agricoles (★★★)
   └── ui/screens/alerts/AlertsScreen.kt

4. Contact expert (★★)
   └── ui/screens/expert/ExpertScreen.kt

5. À propos (★)
   └── ui/screens/about/AboutScreen.kt
```

---

## 🧪 Comment tester ce qui a été fait

### Test 1 — Compilation ✅
```powershell
cd C:\GREMAHTECH\GremahTech\PFE\AgriPredict\mobile
.\gradlew.bat clean assembleDebug
```
**Résultat attendu :** `BUILD SUCCESSFUL` ✅ (confirmé le 10 mars 2026)

### Test 2 — Lancer l'application sur émulateur/appareil
1. Ouvrir Android Studio
2. **File → Sync Project with Gradle Files**
3. Sélectionner un émulateur API 26+ ou un appareil physique
4. Cliquer sur ▶️ Run

### Test 3 — Flux d'authentification
```
Démarrage → Écran Connexion (première utilisation)
    → "Pas de compte ? S'inscrire" → Remplir formulaire → "S'inscrire"
    → Redirection vers Accueil → "Bienvenue, [Nom] 👋"
    → 👤 Profil → Infos + Déconnexion
    → Retour Connexion → Saisir téléphone → Connexion → Accueil
```

### Test 4 — Diagnostic IA + Sauvegarde (⚡ corrigé)
```
Accueil → Diagnostic → Photo/Galerie → Aperçu → Analyser → Résultat
    → "Enregistrer" → ✅ Succès (plus de crash FK !)
    → "Nouveau diagnostic"
```

### Test 5 — Navigation + Langue + Thème sombre
```
Vérifier chaque lien Accueil ↔ écrans
Paramètres → Changer langue (FR/EN/HA/DJE)
Mode sombre Android → Thème s'adapte
```

---

## ⚠️ Problèmes connus

### 1. Erreurs rouges dans l'IDE (faux positifs)
**Solution :** `File → Sync Project with Gradle Files` ou `Invalidate Caches`

### 2. TensorFlow Lite Support désactivé temporairement
**Cause :** Conflit namespace AGP 9.0.1. Utilisation de `tensorflow-lite` directement.

### 3. Bug preprocessing TFLite — RÉSOLU ✅
**Solution :** Pixels bruts [0,255] float32 — couche Rescaling intégrée au modèle.

### 4. Warning expérimental KSP — Impact nul

### 5. fallbackToDestructiveMigration actif (phase dev uniquement)
**Impact :** Données perdues si le schéma DB change. À remplacer par des migrations en prod.

---

## 🏥 Santé du projet

### Bilan de l'audit du 10 mars 2026

| Critère | Avant audit | Après audit |
|---------|-------------|-------------|
| Build | ✅ PASS | ✅ PASS |
| Erreurs compilation | 0 | 0 |
| Sauvegarde diagnostic | ❌ CRASH FK | ✅ CORRIGÉ |
| Démarrage app | ⚠️ Fragile | ✅ ROBUSTE |
| Auth (toutes fonctions) | ⚠️ Sans protection | ✅ ROBUSTE |
| Historique | ⚠️ Sans protection | ✅ ROBUSTE |
| historyViewModelFactory | ❌ MANQUANT | ✅ AJOUTÉ |
| Architecture Clean Arch | ✅ Solide | ✅ Solide |
| MVVM | ✅ Solide | ✅ Solide |
| Offline-first | ✅ Bon | ✅ Bon |
| i18n | ✅ Complet | ✅ Complet |

### Score de santé global : 🟢 8.5/10

**Points forts :**
- Architecture propre (Clean Architecture + MVVM)
- 63 fichiers Kotlin bien organisés (~5 500 lignes)
- Gestion d'erreurs robuste (après corrections)
- i18n 4 langues, 0 texte en dur
- Transaction Room atomique pour sauvegarde multi-table
- Modèle IA fonctionnel

**Points à améliorer (non-bloquants) :**
- 5 écrans placeholder à remplir (Phase 7)
- Sync non implémentée (Phase 8)
- Pas de tests unitaires (Phase 10)
- `fallbackToDestructiveMigration` à remplacer en production

---

## 📊 Roadmap visuelle

```
Étape                              Statut       Priorité
────────────────────────────────────────────────────────
 1. Setup projet + Gradle           ✅ FAIT      —
 2. Architecture Clean Arch         ✅ FAIT      —
 3. Base de données Room            ✅ FAIT      —
 4. DTOs synchronisation            ✅ FAIT      —
 5. Sync offline-first (squelette)  ✅ FAIT      —
 6. UI Home + Navigation            ✅ FAIT      —
 7. Thème Material 3 agricole       ✅ FAIT      —
 8. i18n (4 langues)                ✅ FAIT      —
 9. Diagnostic IA (caméra+galerie)  ✅ FAIT      —
10. Authentification complète       ✅ FAIT      —
11. Modèle IA déployé + bug fix     ✅ FAIT      —
12. HistoryViewModel                ✅ FAIT      —
13. ⚡ Audit & correction bugs      ✅ FAIT      — ← NOUVEAU
────────────────────────────────────────────────────────
14. 🎯 Historique UI + Détail       🔲 NEXT     ★★★★★
15. Base de connaissances maladies  🔲           ★★★★
16. Alertes agricoles               🔲           ★★★
17. Contact expert                  🔲           ★★
18. À propos                        🔲           ★
19. Sync complète + Backend         🔲           ★★★★
20. Tests                           🔲           ★★★
21. Finitions + polish              🔲           ★★
────────────────────────────────────────────────────────
```

---

## 📁 Fichiers clés à connaître

| Fichier | Rôle |
|---------|------|
| `AppContainer.kt` | DI : Database, DAOs, Repos, IA, Auth, History, ViewModels |
| `AgriPredictDatabase.kt` | 9 tables Room |
| `DiagnosticRepositoryImpl.kt` | Transaction Room, 3 DAOs, ordre FK correct |
| `DiagnosticViewModel.kt` | 6 états, analyse IA, sauvegarde avec vérification userId |
| `AuthViewModel.kt` | Auth complète (try-catch robuste dans toutes les fonctions) |
| `HistoryViewModel.kt` | Historique (try-catch, état Error) |
| `TFLiteClassifier.kt` | Moteur IA TFLite + mode démo |
| `SessionPreferences.kt` | DataStore session (userId, isLoggedIn) |
| `AgriPredictNavGraph.kt` | 12 routes + auth conditionnelle |

---

## 📈 Statistiques du projet

| Métrique | Valeur |
|----------|--------|
| Fichiers Kotlin | 63 fichiers |
| Lignes de code Kotlin | ~5 500 lignes |
| Tables Room | 9 |
| DAOs | 9 |
| Écrans UI | 11 (dont 5 placeholders) |
| ViewModels | 3 |
| Langues | 4 (FR, EN, HA, DJE) |
| Routes navigation | 12 |
| Bugs critiques corrigés | 2 (FK order + userId fallback) |
| Bugs moyens corrigés | 3 (try-catch + factory manquant) |
| Build status | ✅ BUILD SUCCESSFUL (10 mars 2026) |
| Erreurs compile | 0 |

---

## 👨‍🎓 Notes pour la soutenance

- **Architecture** : Clean Architecture — séparation data/domain/ui
- **MVVM** : StateFlow/collectAsState
- **Offline-first** : Room + DataStore, sync PENDING/SYNCED/FAILED
- **Transaction Room** : Sauvegarde multi-table atomique (Diagnostic→Image→Prediction)
- **Foreign Keys** : Insertions parent→enfant respectées
- **Gestion d'erreurs** : try-catch dans tous les ViewModels
- **i18n** : 4 langues sans modifier le code source
- **DI manuelle** : Choix pédagogique (plus simple que Hilt)
- **Modèle IA** : MobileNetV2 INT8 quantifié, 24 classes
- **Preprocessing** : Pixels bruts [0,255] — Rescaling intégrée au modèle

---

> 📌 **Prochain objectif :** Implémenter l'UI de l'Historique des diagnostics
> Le ViewModel est prêt — il faut remplir `HistoryScreen.kt` (LazyColumn) et créer `HistoryDetailScreen.kt`
