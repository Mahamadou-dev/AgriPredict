# 🌾 AgriPredict — Récapitulatif Global de Cohérence

> **Analyse transversale complète du projet**
> Projet de fin d'études (PFE) — Licence en Génie Logiciel
> Date de l'analyse : 14 mars 2026 — **Post-refactoring Parcelles**

---

## 📋 Table des matières

1. [Vue d'ensemble du projet](#1--vue-densemble-du-projet)
2. [Composants du projet](#2--composants-du-projet)
3. [Analyse de cohérence Mobile ↔ Backend](#3--analyse-de-cohérence-mobile--backend)
4. [Analyse de cohérence IA ↔ Mobile ↔ Backend](#4--analyse-de-cohérence-ia--mobile--backend)
5. [Points de cohérence validés ✅](#5--points-de-cohérence-validés-)
6. [Incohérences et points d'attention ⚠️](#6--incohérences-et-points-dattention-%EF%B8%8F)
7. [État d'avancement global](#7--état-davancement-global)
8. [Matrice de dépendances inter-composants](#8--matrice-de-dépendances-inter-composants)
9. [Plan d'intégration recommandé](#9--plan-dintégration-recommandé)
10. [Score de cohérence global](#10--score-de-cohérence-global)

---

## 1. 🔭 Vue d'ensemble du projet

**AgriPredict** est une solution complète d'aide au diagnostic de maladies des plantes destinée aux agriculteurs d'Afrique de l'Ouest. Le projet se compose de **4 parties** :

| Composant | Technologie | État | Score |
|-----------|-------------|------|-------|
| 📱 **Mobile** (Android) | Kotlin + Jetpack Compose + TFLite | ✅ Avancé (Phase 8) | 🟢 9.5/10 |
| 🖥️ **Backend** (API REST) | Python + FastAPI + PostgreSQL | ✅ Complet + Parcelles | 🟢 9/10 |
| 🧠 **IA / Recherche** | TensorFlow + MobileNetV2 | ✅ Modèle entraîné | 🟡 7/10 |
| 🌐 **Web Dashboard** | Non défini | 🔲 Non commencé | ⬛ 0/10 |
| 📄 **Documentation** | LaTeX + PDF | ⚠️ Partiel | 🟡 5/10 |

---

## 2. 📦 Composants du projet

### 2.1 📱 Application Mobile (Kotlin / Android)

**Rôle :** Application utilisée par les agriculteurs sur le terrain.

| Aspect | Détail |
|--------|--------|
| Architecture | Clean Architecture + MVVM |
| Base de données locale | Room (10 tables, 10 DAOs) — v3 avec ParcelleEntity |
| IA embarquée | TFLite MobileNetV2 INT8 (24 classes, 2.87 Mo) |
| UI | Jetpack Compose + Material 3, 14 écrans fonctionnels |
| Offline-first | Room + DataStore + SyncStatus (PENDING/SYNCED/FAILED) |
| i18n | 4 langues (FR, EN, Hausa, Zarma) — ~205 clés/langue |
| Auth | Locale (Room + DataStore) — inscription simplifiée (nomPrenom, tel, mdp) |
| Parcelles | 🆕 CRUD complet, sélection obligatoire avant diagnostic, flux post-inscription |
| Sync | Squelette prêt (SyncManager/SyncWorker) — DTOs alignés avec backend |
| Données pré-chargées | 24 maladies, 41 traitements, 6 alertes (DatabaseSeeder) |

### 2.2 🖥️ Backend API (FastAPI / Python)

**Rôle :** Centraliser les données, distribuer les mises à jour, gérer les utilisateurs.

| Aspect | Détail |
|--------|--------|
| Architecture | Modulaire (core / models / schemas / crud / api / services) |
| Base de données | PostgreSQL 16 (13 entités SQLAlchemy, + Parcelle) |
| Auth | JWT (python-jose) + bcrypt, inscription simplifiée (nomPrenom) |
| Endpoints | 40+ routes REST (+ CRUD Parcelles) |
| Sync mobile | Routes doubles (/sync/* + /api/*), sync profil + parcelles |
| Rôles | AGRICULTEUR, EXPERT, ADMIN |
| Dockerisé | docker-compose (API + PostgreSQL) |
| Documentation | Swagger UI auto-généré (/docs) |
| Données pré-chargées | 24 maladies, 41 traitements, 6 alertes (seeder.py) |

### 2.3 🧠 Recherche IA

**Rôle :** Entraîner le modèle de classification des maladies de plantes.

| Aspect | Détail |
|--------|--------|
| Modèle | MobileNetV2 (notebook V2) + MobileNetV3 (notebook V3) |
| Dataset | 24 classes (5 manioc, 4 maïs, 2 poivron, 3 pomme de terre, 10 tomate) |
| Format déployé | TFLite INT8 quantifié |
| Précision annoncée | ~95% |
| Preprocessing | Pixels [0, 255] float32, Rescaling intégré au modèle |
| Input | 224×224 pixels |

### 2.4 🌐 Web Dashboard

**Rôle :** Interface d'administration pour experts et admins.

| Aspect | Détail |
|--------|--------|
| État | 🔲 **Dossier vide** — Aucun code |
| Stack prévue | Non définie |

### 2.5 📄 Documentation

| Fichier | Contenu |
|---------|---------|
| `Cahier_des_charges_AgriPredict.pdf` | Spécifications fonctionnelles |
| `Conception.pdf` | Architecture et design |
| `rapport_modele_agripredict.tex` | Rapport sur le modèle IA |
| `bug_preprocessing_tflite.md` | Documentation du bug de preprocessing résolu |

---

## 3. 🔗 Analyse de cohérence Mobile ↔ Backend

### 3.1 ✅ Alignement des DTOs de synchronisation

C'est l'un des points les plus critiques. Les DTOs doivent avoir des champs **identiques** entre le Kotlin (mobile) et le Python (backend).

#### UPLINK (Mobile → Serveur)

| DTO | Champ | Mobile (Kotlin) | Backend (Python) | Cohérent ? |
|-----|-------|-----------------|------------------|------------|
| **UserSyncDTO** | id | `String` | `str` | ✅ |
| | nomPrenom | `String` | `str` | ✅ 🔄 |
| | telephone | `String` | `str` | ✅ |
| | parcelles | `List<ParcelleSyncDTO>` | `list[ParcelleSyncDTO]` | ✅ 🆕 |
| **ParcelleSyncDTO** | id | `String` | `str` | ✅ 🆕 |
| | nomParcelle | `String` | `str` | ✅ 🆕 |
| | commune | `String = ""` | `str = ""` | ✅ 🆕 |
| | village | `String = ""` | `str = ""` | ✅ 🆕 |
| | ville | `String = ""` | `str = ""` | ✅ 🆕 |
| **DiagnosticUploadDTO** | id | `String` | `str` | ✅ |
| | userId | `String` | `str` | ✅ |
| | date | `Long` | `int` (ms) | ✅ |
| | parcelleId | `String?` | `str \| None` | ✅ 🆕 |
| | location | `LocationUploadDTO?` | `LocationUploadDTO \| None` | ✅ |
| | prediction | `PredictionUploadDTO?` | `PredictionUploadDTO \| None` | ✅ |
| **LocationUploadDTO** | id | `String` | `str` | ✅ |
| | latitude | `Double` | `float` | ✅ |
| | longitude | `Double` | `float` | ✅ |
| | region | `String = ""` | `str = ""` | ✅ |
| | village | `String = ""` | `str = ""` | ✅ |
| **PredictionUploadDTO** | id | `String` | `str` | ✅ |
| | label | `String` | `str` | ✅ |
| | confidence | `Float` | `float` | ✅ |
| | modelVersion | `String = ""` | `str = ""` | ✅ |
| | maladieId | `Int?` | `int \| None` | ✅ |
| **DiagnosticUploadResponseDTO** | success | `Boolean` | `bool` | ✅ |
| | diagnosticId | `String` | `str` | ✅ |
| | message | `String = ""` | `str = ""` | ✅ |

**Verdict UPLINK : ✅ 100% cohérent — UserSyncDTO refactorisé avec parcelles[], DiagnosticUploadDTO enrichi avec parcelleId.**

#### DOWNLINK (Serveur → Mobile)

| DTO | Champ | Mobile (Kotlin) | Backend (Python) | Cohérent ? |
|-----|-------|-----------------|------------------|------------|
| **CheckUpdatesResponseDTO** | hasUpdate | `Boolean` | `bool` | ✅ |
| | knowledgeBaseVersion | `String = ""` | `str = ""` | ✅ |
| | modelVersion | `String = ""` | `str = ""` | ✅ |
| | appConfigVersion | `String = ""` | `str = ""` | ✅ |
| **MaladieDTO** | id | `Int` | `int` | ✅ |
| | nomCommun | `String` | `str` | ✅ |
| | nomScientifique | `String = ""` | `str = ""` | ✅ |
| | description | `String = ""` | `str = ""` | ✅ |
| **TraitementDTO** | id | `Int` | `int` | ✅ |
| | titre | `String` | `str` | ✅ |
| | description | `String = ""` | `str = ""` | ✅ |
| | dosage | `String = ""` | `str = ""` | ✅ |
| | maladieId | `Int` | `int` | ✅ |
| **AlerteDTO** | id | `String` | `str` | ✅ |
| | message | `String` | `str` | ✅ |
| | zone | `String = ""` | `str = ""` | ✅ |
| | gravite | `Float = 0f` | `float = 0.0` | ✅ |
| | dateEmission | `Long` | `int` (ms) | ✅ |
| | dateExpiration | `Long?` | `int \| None` | ✅ |
| | maladieId | `Int?` | `int \| None` | ✅ |
| **ModelUpdateDTO** | version | `String` | `str` | ✅ |
| | downloadUrl | `String` | `str` | ✅ |
| | framework | `String = "tflite"` | `str = "tflite"` | ✅ |
| | precision | `Float = 0f` | `float = 0.0` | ✅ |
| | inputSize | `Int = 224` | `int = 224` | ✅ |
| | checksum | `String = ""` | `str = ""` | ✅ |
| **AppConfigDTO** | version | `String` | `str` | ✅ |
| | syncIntervalMinutes | `Int = 60` | `int = 60` | ✅ |
| | maxImageSizeMb | `Int = 5` | `int = 5` | ✅ |
| | features | `Map<String, Boolean>` | `dict[str, bool]` | ✅ |
| **UpdateBundleDTO** | knowledgeBase | `KnowledgeBaseDTO?` | `KnowledgeBaseDTO \| None` | ✅ |
| | alertes | `List<AlerteDTO>` | `list[AlerteDTO]` | ✅ |
| | modelUpdate | `ModelUpdateDTO?` | `ModelUpdateDTO \| None` | ✅ |
| | appConfig | `AppConfigDTO?` | `AppConfigDTO \| None` | ✅ |

**Verdict DOWNLINK : ✅ 100% cohérent — Tous les champs sont alignés.**

---

### 3.2 ✅ Alignement des endpoints API

| Endpoint Mobile (Retrofit) | Endpoint Backend | Méthode | Cohérent ? |
|-----------------------------|------------------|---------|------------|
| `POST api/users/sync` | `/api/users/sync` + `/sync/profile` | POST | ✅ |
| `POST api/diagnostics/upload` | `/api/diagnostics/upload` + `/sync/diagnostics` | POST | ✅ |
| `GET api/updates/check` | `/api/updates/check` + `/sync/check-updates` | GET | ✅ |
| `GET api/updates/download` | `/api/updates/download` + `/sync/download` | GET | ✅ |

**Verdict Endpoints : ✅ 100% cohérent — Routes doubles sur le backend pour compatibilité.**

> 💡 **Note :** Le backend expose des routes dupliquées (`/sync/*` et `/api/*`) pour assurer une flexibilité maximale. Le mobile utilise les routes `/api/*`.

---

### 3.3 ✅ Alignement du modèle de données (Entités)

| Entité | Mobile (Room) | Backend (SQLAlchemy) | Cohérent ? | Notes |
|--------|--------------|---------------------|------------|-------|
| **Utilisateur** | `UserEntity` (nomPrenom, tel, mdpHash) | `User` + `Farmer` (héritage) | ✅ 🔄 | Simplifié des deux côtés, commune/village → Parcelle |
| **Parcelle** | `ParcelleEntity` (FK→User) | `Parcelle` (FK→Farmer) | ✅ 🆕 | Nouveau ! nomParcelle, commune, village, ville |
| **Diagnostic** | `DiagnosticEntity` (+ parcelleId) | `Diagnostic` (+ parcelleId) | ✅ 🔄 | FK → Parcelle ajoutée des deux côtés |
| **Image** | `ImageEntity` | `Image` | ✅ | Mêmes champs (id, path, diagnosticId) |
| **Location** | `LocationEntity` | `Location` | ✅ | Mêmes champs (id, lat, lon, region, village) |
| **Prediction** | `PredictionEntity` | `PredictionResult` | ✅ | Mêmes champs (id, label, confidence, modelVersion, maladieId) |
| **Maladie** | `MaladieEntity` (id: Int) | `Maladie` (id: Int, fixe) | ✅ | IDs identiques (1-24), même structure |
| **Traitement** | `TraitementEntity` (id: Int) | `Traitement` (id: Int, fixe) | ✅ | IDs identiques (1-41), même structure |
| **Alerte** | `AlerteEntity` (id: String) | `Alert` (id: UUID) | ✅ | UUID compatible String ↔ UUID |
| **ModeleIA** | `ModeleIAEntity` | `ModeleIA` | ✅ | Mêmes champs (version, framework, precision, inputSize) |

**Verdict Entités : ✅ Cohérent à 97%** — Parcelle ajoutée des deux côtés. User simplifié de manière alignée. Seuls Expert/Admin n'existent que côté backend (logique).

---

### 3.4 ✅ Alignement des données pré-chargées (Seeder)

| Donnée | Mobile (DatabaseSeeder.kt) | Backend (seeder.py) | Cohérent ? |
|--------|---------------------------|---------------------|------------|
| Maladies | 24 maladies (IDs 1-24) | 24 maladies (IDs 1-24) | ✅ |
| Traitements | 41 traitements (IDs 1-41) | 41 traitements (IDs 1-41) | ✅ |
| Alertes | 6 alertes (Niger) | 6 alertes (Niger) | ✅ |
| Ordre des classes | Manioc(5) → Maïs(4) → Poivron(2) → PdT(3) → Tomate(10) | Identique | ✅ |
| IDs fixes | Oui (pas d'auto-increment) | Oui (`autoincrement=False`) | ✅ |

**Verdict Seeder : ✅ 100% cohérent — Données miroir entre mobile et backend.**

---

### 3.5 Alignement de l'authentification

| Aspect | Mobile | Backend | Cohérent ? |
|--------|--------|---------|------------|
| Méthode | 🟡 **Locale** (Room) | ✅ JWT (python-jose + bcrypt) | ⚠️ **Découplé** |
| Inscription | Room insert (nomPrenom, tel, mdp) | `/api/auth/register` (nomPrenom, tel, password) | ✅ **Champs alignés** |
| Connexion | Room query local | `/api/auth/login` → JWT | ⚠️ **Non connectés** |
| Profil | Room local (nomPrenom, tel) | `/api/auth/me` (GET/PUT : nom, tel) | ✅ **Champs alignés** |
| Parcelles post-inscription | 🆕 Écran AddParcelle | 🆕 `/api/parcelles/{id}` | ✅ **Flux aligné** |
| Changer MDP | Room local | `/api/auth/change-password` | ⚠️ **Non connectés** |

**Verdict Auth : ⚠️ Découplé mais aligné** — Les champs et le flux sont identiques. L'authentification fonctionne en local côté mobile mais les contrats d'inscription sont prêts pour l'intégration REST.

> 💡 **Amélioration apportée** : L'inscription est maintenant simplifiée des deux côtés (nomPrenom + telephone + password). Les parcelles sont ajoutées séparément après l'inscription, ce qui est cohérent avec le flux UX mobile (inscription → ajout parcelle → accueil).

---

## 4. 🧠 Analyse de cohérence IA ↔ Mobile ↔ Backend

### 4.1 Classes du modèle IA

| # | Classe (label IA) | Mobile (labels.txt) | Mobile (BDD) | Backend (BDD) | Cohérent ? |
|---|-------------------|--------------------|--------------|--------------|----|
| 0 | cassava___bacterial_blight_(cbb) | ✅ | Maladie ID=1 | Maladie ID=1 | ✅ |
| 1 | cassava___brown_streak_disease_(cbsd) | ✅ | Maladie ID=2 | Maladie ID=2 | ✅ |
| 2 | cassava___green_mottle_(cgm) | ✅ | Maladie ID=3 | Maladie ID=3 | ✅ |
| 3 | cassava___healthy | ✅ | Maladie ID=4 | Maladie ID=4 | ✅ |
| 4 | cassava___mosaic_disease_(cmd) | ✅ | Maladie ID=5 | Maladie ID=5 | ✅ |
| 5 | Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot | ✅ | Maladie ID=6 | Maladie ID=6 | ✅ |
| 6 | Corn_(maize)___Common_rust_ | ✅ | Maladie ID=7 | Maladie ID=7 | ✅ |
| 7 | Corn_(maize)___healthy | ✅ | Maladie ID=8 | Maladie ID=8 | ✅ |
| 8 | Corn_(maize)___Northern_Leaf_Blight | ✅ | Maladie ID=9 | Maladie ID=9 | ✅ |
| 9 | Pepper,_bell___Bacterial_spot | ✅ | Maladie ID=10 | Maladie ID=10 | ✅ |
| 10 | Pepper,_bell___healthy | ✅ | Maladie ID=11 | Maladie ID=11 | ✅ |
| 11 | Potato___Early_blight | ✅ | Maladie ID=12 | Maladie ID=12 | ✅ |
| 12 | Potato___healthy | ✅ | Maladie ID=13 | Maladie ID=13 | ✅ |
| 13 | Potato___Late_blight | ✅ | Maladie ID=14 | Maladie ID=14 | ✅ |
| 14 | Tomato___Bacterial_spot | ✅ | Maladie ID=15 | Maladie ID=15 | ✅ |
| 15 | Tomato___Early_blight | ✅ | Maladie ID=16 | Maladie ID=16 | ✅ |
| 16 | Tomato___healthy | ✅ | Maladie ID=17 | Maladie ID=17 | ✅ |
| 17 | Tomato___Late_blight | ✅ | Maladie ID=18 | Maladie ID=18 | ✅ |
| 18 | Tomato___Leaf_Mold | ✅ | Maladie ID=19 | Maladie ID=19 | ✅ |
| 19 | Tomato___Septoria_leaf_spot | ✅ | Maladie ID=20 | Maladie ID=20 | ✅ |
| 20 | Tomato___Spider_mites Two-spotted_spider_mite | ✅ | Maladie ID=21 | Maladie ID=21 | ✅ |
| 21 | Tomato___Target_Spot | ✅ | Maladie ID=22 | Maladie ID=22 | ✅ |
| 22 | Tomato___Tomato_mosaic_virus | ✅ | Maladie ID=23 | Maladie ID=23 | ✅ |
| 23 | Tomato___Tomato_Yellow_Leaf_Curl_Virus | ✅ | Maladie ID=24 | Maladie ID=24 | ✅ |

**Verdict IA ↔ Mobile ↔ Backend : ✅ 100% cohérent — Les 24 classes, labels et IDs sont parfaitement alignés sur les 3 couches.**

### 4.2 Pipeline IA complet

```
[Photo plante] 
    → TFLiteClassifier (mobile, 224×224, [0-255] float32)
    → Label brut (ex: "Tomato___Early_blight")
    → LabelFormatter → Plante + Maladie lisible
    → MaladieRepositoryImpl.findByLabel() → Maladie ID=16 
    → TraitementDao.getByMaladieId(16) → Liste traitements
    → Affichage UI (DiagnosticScreen + HistoryDetailScreen)
```

| Étape | Mobile | Backend | Cohérent ? |
|-------|--------|---------|------------|
| Modèle IA | MobileNetV2 INT8 embarqué | Référencé dans `modeles_ia` (v1.0.0) | ✅ |
| Preprocessing | Pixels [0,255] float32 | N/A (pas d'inférence serveur) | ✅ |
| Labels | `labels.txt` (24 lignes) | Correspondance via `maladies.id` | ✅ |
| Mapping label→maladie | `MaladieRepositoryImpl` (table de mapping) | Mapping implicite via IDs fixes | ✅ |
| Traitements | BDD locale (41 traitements) | BDD serveur (41 traitements identiques) | ✅ |

---

## 5. ✅ Points de cohérence validés

### Architecture et Design

| Point de cohérence | Détail | Score |
|--------------------|--------|-------|
| **DTOs parfaitement alignés** | 14 DTOs identiques (+ ParcelleSyncDTO) en champs, types et défauts | ✅ 10/10 |
| **Endpoints compatibles** | 4 routes sync + 4 routes CRUD parcelles + auth aligné | ✅ 10/10 |
| **Modèle de données cohérent** | 10 entités mobile ↔ 13 entités backend (+ Parcelle des 2 côtés) | ✅ 9.5/10 |
| **Parcelles alignées** | 🆕 Même structure mobile↔backend, FK diagnostic→parcelle | ✅ 10/10 |
| **IDs fixes synchronisés** | Maladies (1-24) et Traitements (1-41) identiques | ✅ 10/10 |
| **Données seeder miroir** | 24 maladies + 41 traitements + 6 alertes identiques | ✅ 10/10 |
| **24 classes IA alignées** | labels.txt ↔ mapping mobile ↔ seeder backend | ✅ 10/10 |
| **Flux inscription aligné** | Mobile : nomPrenom+tel+mdp → Backend : nomPrenom+tel+password | ✅ 10/10 |
| **Offline-first cohérent** | SyncStatus + squelette WorkManager + DTOs prêts | ✅ 8/10 |
| **Architecture pédagogique** | Les deux côtés privilégient la simplicité et la lisibilité | ✅ 10/10 |

### Flux de données

| Flux | Mobile | Backend | Statut |
|------|--------|---------|--------|
| Inscription | Room local | POST /api/auth/register | ⚠️ Non connecté |
| Connexion | Room local | POST /api/auth/login → JWT | ⚠️ Non connecté |
| Sync profil | UserSyncDTO → Retrofit | POST /sync/profile | ⚠️ Non connecté |
| Upload diagnostic | DiagnosticUploadDTO → Retrofit | POST /sync/diagnostics | ⚠️ Non connecté |
| Check updates | Retrofit → CheckUpdatesResponseDTO | GET /sync/check-updates | ⚠️ Non connecté |
| Download bundle | Retrofit → UpdateBundleDTO | GET /sync/download | ⚠️ Non connecté |
| Base connaissances | DatabaseSeeder local | GET /sync/knowledge-base | ⚠️ Non connecté |
| Alertes | DatabaseSeeder local | GET /sync/alerts | ⚠️ Non connecté |

---

## 6. ⚠️ Incohérences et points d'attention

### 🔴 Critiques (à corriger avant la soutenance)

| # | Incohérence | Impact | Recommandation |
|---|-------------|--------|----------------|
| **C1** | **Auth mobile 100% locale, pas connectée au backend** | Fonctionnel mais isolé | L'inscription/connexion mobile n'appelle aucun endpoint backend. Les champs sont maintenant alignés (nomPrenom, telephone, password). Implémenter au minimum un `POST /api/auth/register` depuis le mobile, ou documenter que l'auth est offline-only par design. |
| **C2** | **Upload d'image non implémenté** | Le diagnostic ne transmet pas la photo au serveur | Le DTO `DiagnosticUploadDTO` ne contient pas le binaire de l'image. Le backend a un champ `Image.path` mais pas d'endpoint multipart/form-data. Ajouter un endpoint d'upload ou utiliser Base64. |

### 🟡 Moyens (améliorations recommandées)

| # | Point d'attention | Impact | Recommandation |
|---|-------------------|--------|----------------|
| **M1** | **SyncManager/SyncWorker sont des squelettes vides** | La sync ne fonctionne pas réellement | Implémenter au minimum une sync manuelle (bouton "synchroniser" dans l'app) |
| **M2** | **Pas de base URL Retrofit configurée** | Le mobile ne sait pas où envoyer les requêtes | Ajouter la base URL (`http://10.0.2.2:8000/` pour émulateur, IP réelle pour device) dans la config |
| **M3** | **Le mobile n'utilise pas JWT** | Pas d'authentification sur les requêtes API | Ajouter un intercepteur OkHttp qui injecte le token JWT dans les headers |
| **M4** | **Backend : endpoints sync sans auth** | Les routes /sync/* n'exigent aucun token | Sécuriser avec au minimum un token API ou JWT |
| **M5** | **Backend : `SECRET_KEY` par défaut dans docker-compose** | Risque en production | Utiliser une variable d'environnement aléatoire |
| **M6** | **Mobile : `fallbackToDestructiveMigration`** | Perte de données si le schéma Room change | Remplacer par des migrations Room propres avant release |
| **M7** | **Pas de gestion de conflit** | Si le même diagnostic est modifié offline et online | Implémenter une stratégie "last-write-wins" ou versioning |
| **M8** | **README IA vide** | Pas de documentation du composant recherche | Compléter le README de ai-research avec les résultats d'entraînement |

### 🟢 Mineurs (cosmétiques)

| # | Point | Recommandation |
|---|-------|----------------|
| **m1** | Le web-dashboard est vide | Si c'est dans le scope du PFE, le mentionner en perspective |
| **m2** | Pas de tests unitaires côté mobile | Ajouter au moins quelques tests ViewModel |
| **m3** | Tests backend minimaux | Étoffer avec des tests d'intégration (auth + sync) |
| **m4** | Le backend n'a pas d'endpoint pour le modèle TFLite | Le `ModelUpdateDTO.downloadUrl` pointe vers rien |

---

## 7. 📊 État d'avancement global

### Par composant

```
Composant           Progression     Phases terminées
──────────────────────────────────────────────────────
📱 Mobile           █████████████░  90%  (Phase 1-8 / 10)
🖥️ Backend          █████████████░  92%  (Phase 1-10 / 10)
🧠 IA Research      ████████░░░░░░  60%  (Modèle entraîné, doc manquante)
🌐 Web Dashboard    ░░░░░░░░░░░░░░   0%  (Non commencé)
📄 Documentation    ██████░░░░░░░░  50%  (Partielle)
🔗 Intégration M↔B  ████░░░░░░░░░░  25%  (DTOs + parcelles alignés, non connecté)
──────────────────────────────────────────────────────
TOTAL PROJET        █████████░░░░░  68%
```

### Par fonctionnalité

| Fonctionnalité | Mobile | Backend | Intégré ? | Global |
|----------------|--------|---------|-----------|--------|
| Auth (inscription/connexion) | ✅ Local (simplifié) | ✅ JWT (simplifié) | ❌ Non | 🟡 65% |
| Parcelles agricoles | ✅ CRUD complet | ✅ CRUD complet | ❌ Non sync | 🟡 75% |
| Diagnostic IA | ✅ TFLite + parcelle | N/A (embarqué) | ✅ Autonome | 🟢 95% |
| Base de connaissances | ✅ Seeder local | ✅ CRUD + Seeder | ❌ Non sync | 🟡 70% |
| Alertes | ✅ Seeder local | ✅ CRUD | ❌ Non sync | 🟡 70% |
| Historique diagnostics | ✅ Room local | ✅ CRUD | ❌ Non sync | 🟡 60% |
| Sync offline-first | ⚠️ Squelette | ✅ Routes prêtes | ❌ Non connecté | 🔴 20% |
| Profil utilisateur | ✅ Local | ✅ CRUD | ❌ Non connecté | 🟡 65% |
| Gestion experts/admins | N/A | ✅ CRUD + rôles | N/A | 🟢 90% |
| Contact expert | ✅ Annuaire local | N/A | ✅ Autonome | 🟢 90% |
| i18n (4 langues) | ✅ 205 clés | N/A | ✅ Autonome | 🟢 100% |
| UI/UX | ✅ 14 écrans complets | ✅ Swagger | ✅ Indépendants | 🟢 95% |
| Docker/Déploiement | N/A | ✅ Compose | N/A | 🟢 90% |

---

## 8. 🗺️ Matrice de dépendances inter-composants

```
                    Mobile      Backend     IA Research   Web Dashboard
                  ┌──────────┬──────────┬──────────────┬──────────────┐
  Mobile          │    —     │ DTOs ✅   │ TFLite ✅     │     —        │
                  │          │ REST ⚠️   │ labels ✅     │              │
                  │          │ Auth ❌   │ model ✅      │              │
  ────────────────┼──────────┼──────────┼──────────────┼──────────────┤
  Backend         │ DTOs ✅   │    —     │ Seeder ✅     │ REST API ⚠️  │
                  │ Sync ⚠️  │          │ IDs ✅        │ (pas de      │
                  │ Auth ❌  │          │              │  client)     │
  ────────────────┼──────────┼──────────┼──────────────┼──────────────┤
  IA Research     │ Model ✅  │ IDs ✅   │     —        │     —        │
                  │ Labels ✅ │ Seeder ✅ │              │              │
  ────────────────┼──────────┼──────────┼──────────────┼──────────────┤
  Web Dashboard   │    —     │ REST ❌  │     —        │     —        │
  └──────────────┴──────────┴──────────┴──────────────┴──────────────┘
  
  ✅ = Intégré et fonctionnel
  ⚠️ = Défini/préparé mais pas connecté
  ❌ = Non implémenté
```

---

## 9. 🛣️ Plan d'intégration recommandé

### Priorité 1 : Connexion Mobile ↔ Backend (★★★★★)

```
Étape 1.1 — Configuration Retrofit (1h)
    └── Ajouter base URL dans AppContainer.kt
    └── Créer OkHttpClient avec intercepteur log
    └── Tester avec /health

Étape 1.2 — Auth API depuis le mobile (2-3h)
    └── Modifier AuthViewModel pour appeler POST /api/auth/register
    └── Stocker le JWT dans SessionPreferences
    └── Créer un intercepteur OkHttp qui ajoute Authorization: Bearer <token>
    └── Fallback offline : si pas de réseau → auth locale existante

Étape 1.3 — Sync manuelle (2-3h)
    └── Implémenter SyncManager.syncUplink()
        └── Chercher les diagnostics PENDING dans Room
        └── Pour chacun : POST /api/diagnostics/upload
        └── Si succès : mettre à jour syncStatus = SYNCED
    └── Implémenter SyncManager.syncDownlink()
        └── GET /api/updates/download → UpdateBundleDTO
        └── Insérer/mettre à jour maladies, traitements, alertes dans Room
    └── Ajouter un bouton "Synchroniser" dans SettingsScreen

Étape 1.4 — Sync automatique (1-2h)
    └── Implémenter SyncWorker (WorkManager)
    └── Sync périodique toutes les 60 minutes (si réseau)
```

### Priorité 2 : Upload d'image (★★★)

```
Étape 2.1 — Backend : endpoint multipart
    └── POST /api/diagnostics/upload-image (multipart/form-data)
    └── Sauvegarder le fichier + mettre à jour Image.path

Étape 2.2 — Mobile : envoi image
    └── Modifier DiagnosticUploadDTO ou créer un appel séparé
    └── Compresser l'image avant envoi (< 5 Mo)
```

### Priorité 3 : Tests (★★★)

```
Étape 3.1 — Backend : tests intégration (2h)
    └── Test auth (register → login → me)
    └── Test sync (upload diagnostic → check updates → download)
    
Étape 3.2 — Mobile : tests unitaires (2h)
    └── Test AuthViewModel (register, login, logout)
    └── Test DiagnosticViewModel (classify, save)
    └── Test HistoryViewModel (load, delete)
```

### Priorité 4 : Web Dashboard (★★)

```
Étape 4.1 — Setup React/Next.js ou Vue.js
Étape 4.2 — Dashboard admin : visualiser diagnostics, maladies, alertes
Étape 4.3 — Panel expert : valider diagnostics
```

---

## 10. 📈 Score de cohérence global

### Scores par dimension

| Dimension | Score | Commentaire |
|-----------|-------|-------------|
| **DTOs Mobile ↔ Backend** | 🟢 **10/10** | Parfaitement alignés, + ParcelleSyncDTO + parcelleId |
| **Endpoints API** | 🟢 **10/10** | Routes doubles pour compatibilité + CRUD parcelles |
| **Modèle de données** | 🟢 **9.5/10** | Parcelle alignée des 2 côtés, User simplifié en cohérence |
| **Données de référence** | 🟢 **10/10** | 24 maladies + 41 traitements identiques |
| **Pipeline IA** | 🟢 **10/10** | 24 classes alignées sur les 3 couches |
| **Authentification** | 🟡 **6/10** | Champs alignés, flux identique, pas encore connecté |
| **Synchronisation réelle** | 🔴 **2/10** | Squelette côté mobile, routes prêtes côté backend |
| **Upload image** | 🔴 **1/10** | Non implémenté |
| **Tests** | 🟡 **3/10** | Très minimaux des deux côtés |
| **Documentation** | 🟡 **6/10** | RECAP excellents, README IA vide, docs partiels |

### Score global de cohérence : 🟡 **7.0 / 10**

```
Score de cohérence pondéré :

  Contrats (DTOs + Endpoints + Modèle)     [Poids: 30%]  →  9.8/10  ✅ Excellent
  Données de référence (IA + Seeder)        [Poids: 20%]  → 10.0/10  ✅ Parfait
  Intégration réelle (Auth + Sync + Image)  [Poids: 30%]  →  3.0/10  🔴 Non connecté
  Qualité (Tests + Documentation)           [Poids: 20%]  →  4.5/10  🟡 À améliorer
  ─────────────────────────────────────────────────────────────────
  TOTAL PONDÉRÉ                                           →  7.0/10  🟡 Bon potentiel
```

---

## 📋 Résumé exécutif

### ✅ Ce qui est remarquablement bien fait

1. **Contrats d'interface impeccables** : Les 14 DTOs sont identiques entre Kotlin et Python (+ ParcelleSyncDTO).
2. **Pipeline IA complet** : Du modèle TFLite → label → mapping BDD → traitements, tout est connecté côté mobile.
3. **Données de référence miroir** : Les 24 maladies, 41 traitements avec IDs fixes garantissent la cohérence.
4. **Architecture pédagogique** : Clean Architecture (mobile) + Modulaire (backend) — explicable devant jury.
5. **Parcelles agricoles cohérentes** : 🆕 Même entité Parcelle (mobile Room + backend SQLAlchemy), même CRUD, DTOs alignés.
6. **Inscription simplifiée et alignée** : 🆕 nomPrenom + telephone + password des deux côtés, parcelles ajoutées séparément.
7. **UI/UX mobile poussée** : 14 écrans fonctionnels, sélecteur parcelle obligatoire, flux post-inscription.
8. **Offline-first pensé** : SyncStatus dans les entités, squelette WorkManager, DataStore pour les sessions.
9. **i18n exemplaire** : 4 langues, ~205 clés, 0 texte en dur.

### ⚠️ Ce qui reste à faire pour être 100% cohérent

1. **Connecter le mobile au backend** (la fondation est prête, il manque le "câblage")
2. **Implémenter la sync réelle** (même juste un bouton "synchroniser" serait suffisant)
3. **Documenter les choix** (pourquoi auth locale, pourquoi offline-first, etc.)
4. **Ajouter des tests** (même basiques, pour la crédibilité devant jury)
5. **Compléter le README IA** (résultats d'entraînement, métriques, graphiques)

### 💡 Estimation pour une cohérence complète

| Tâche | Effort estimé |
|-------|---------------|
| Config Retrofit + base URL | 30 min |
| Auth mobile → backend (avec fallback offline) | 3h |
| Sync manuelle (bouton + SyncManager) | 3h |
| Upload image basique | 2h |
| Tests basiques (5-6 tests) | 2h |
| README IA | 30 min |
| **TOTAL** | **~11h** |

---

> 📌 **Conclusion** : Le projet AgriPredict démontre une **excellente cohérence architecturale** entre les composants mobile et backend. Le refactoring Parcelles a amélioré la modélisation métier (un agriculteur → plusieurs parcelles → diagnostics par parcelle) et les contrats d'interface (DTOs, endpoints, IDs) sont parfaitement alignés des deux côtés. Le point faible principal est que les deux composants fonctionnent encore **en parallèle sans être connectés**. Environ **10 heures de développement** suffiraient pour réaliser l'intégration complète et atteindre un score de cohérence de 9+/10.

---

*Document mis à jour le 14 mars 2026 — Post-refactoring Parcelles — Analyse de cohérence du projet AgriPredict*

