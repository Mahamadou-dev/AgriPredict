# 🌾 AgriPredict — Récapitulatif Backend

> **Backend API agricole intelligente**
> Projet de fin d'études (PFE) — Licence en Génie Logiciel
> Dernière mise à jour : 13 mars 2026 — **Phase 10 : Refactoring Parcelles + Cohérence Mobile**

---

## 📋 Table des matières

1. [Vision du backend](#-vision-du-backend)
2. [Stack technique](#%EF%B8%8F-stack-technique)
3. [Architecture du backend](#%EF%B8%8F-architecture-du-backend)
4. [Ce qui a été fait](#-ce-qui-a-été-fait)
5. [Cohérence backend ↔ mobile](#-cohérence-backend--mobile)
6. [Endpoints API complets](#-endpoints-api-complets)
7. [Comment tester](#-comment-tester)
8. [Prochaines étapes](#-prochaines-étapes)
9. [Santé du backend](#-santé-du-backend)

---

## 🎯 Vision du backend

Le backend AgriPredict sert à :

- **Centraliser** les données des agriculteurs
- **Synchroniser** les diagnostics depuis les mobiles (offline-first)
- **Distribuer** la base de connaissances (maladies/traitements)
- **Publier** des alertes agricoles régionales
- **Gérer** les utilisateurs (agriculteurs, experts, admins)
- **Exposer** une API REST pédagogique et simple

**Principes fondamentaux :**
- Architecture simple et explicable devant jury
- Contrats DTO alignés avec le mobile
- Code commenté et documenté

---

## ⚙️ Stack technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Langage | Python | 3.12 |
| API | FastAPI | 0.115.8 |
| ORM | SQLAlchemy | 2.0.38 |
| Validation | Pydantic | 2.10.6 |
| Migrations | Alembic | 1.14.1 |
| Base de données | PostgreSQL | 16 |
| Auth | JWT (python-jose) | 3.3.0 |
| Hash mot de passe | passlib + bcrypt | 1.7.4 |
| Serveur | Uvicorn | 0.35.0 |
| Tests | pytest + httpx | 8.3.4 / 0.28.1 |
| Containerisation | Docker / Compose | — |

---

## 🏗️ Architecture du backend

```
backend/
├── app/
│   ├── main.py                 ← App FastAPI + middlewares + routers
│   ├── core/
│   │   ├── config.py           ← Configuration (.env)
│   │   ├── database.py         ← Engine + Session PostgreSQL
│   │   └── security.py         ← JWT + hash/verify password
│   ├── models/
│   │   ├── base.py             ← Base SQLAlchemy
│   │   └── entities.py         ← 13 entités métier (+ Parcelle)
│   ├── schemas/
│   │   ├── auth.py             ← DTOs authentification (simplifié : nomPrenom)
│   │   ├── common.py           ← DTOs publics
│   │   ├── sync.py             ← DTOs synchronisation (+ ParcelleSyncDTO, + parcelleId)
│   │   ├── management.py       ← DTOs admin/expert
│   │   └── parcelles.py        ← 🆕 DTOs parcelles (Create, Update, Response)
│   ├── crud/
│   │   ├── auth.py             ← CRUD utilisateurs (simplifié sans commune/village)
│   │   ├── diagnostics.py      ← CRUD diagnostics (+ parcelle_id)
│   │   ├── knowledge.py        ← CRUD maladies/traitements
│   │   ├── alerts.py           ← CRUD alertes
│   │   ├── models.py           ← CRUD modèles IA
│   │   ├── management.py       ← CRUD admin/expert (simplifié)
│   │   ├── users.py            ← CRUD utilisateurs
│   │   └── parcelles.py        ← 🆕 CRUD parcelles (create, get, update, delete)
│   ├── api/
│   │   ├── deps.py             ← Auth + require_roles
│   │   └── routes/
│   │       ├── auth.py         ← /api/auth/* (register simplifié : nomPrenom)
│   │       ├── users.py        ← /api/users/*
│   │       ├── sync.py         ← /sync/* + /api/* (+ sync parcelles)
│   │       ├── diagnostics.py  ← /api/diagnostics/*
│   │       ├── knowledge.py    ← /api/knowledge/*
│   │       ├── alerts.py       ← /api/alerts/*
│   │       ├── experts.py      ← /api/experts/*
│   │       ├── admins.py       ← /api/admins/*
│   │       └── parcelles.py    ← 🆕 /api/parcelles/* (CRUD)
│   └── services/
│       └── seeder.py           ← Données initiales (24 maladies, 41 traitements)
├── alembic/                    ← Migrations DB
├── tests/                      ← Tests pytest
├── Dockerfile                  ← Image Docker
├── docker-compose.yml          ← API + PostgreSQL
├── requirements.txt            ← Dépendances Python
└── README.md                   ← Documentation
```

---

## ✅ Ce qui a été fait

### Phase 1 — Fondation backend ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| Structure modulaire | ✅ | `app/core/models/schemas/crud/api/services` |
| FastAPI app | ✅ | Health check + routing + CORS + error handlers |
| PostgreSQL + Docker | ✅ | Compose prêt (`api` + `db`) |
| Config centralisée | ✅ | `pydantic-settings` avec `.env` |

### Phase 2 — Modèle de données central ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| User | ✅ | UUID, nom, telephone, email, passwordHash, role, isActive |
| Farmer | ✅ 🔄 | Extension User — supprimé commune/village → relation parcelles |
| Parcelle | ✅ 🆕 | UUID, nomParcelle, commune, village, ville, agriculteurId (FK→Farmer) |
| Expert | ✅ | Extension User avec specialite, matricule, commune |
| Admin | ✅ | Extension User avec niveauAcces |
| Diagnostic | ✅ 🔄 | + parcelleId FK → Parcelle (SET_NULL) |
| Image | ✅ | UUID, path, resolution, timestamp, diagnosticId |
| Location | ✅ | UUID, latitude, longitude, region, village |
| PredictionResult | ✅ | UUID, label, confidence, modelVersion, maladieId, diagnosticId |
| Maladie | ✅ | INT id, nomCommun, nomScientifique, description |
| Traitement | ✅ | INT id, titre, description, dosage, maladieId |
| Alert | ✅ | UUID, message, zone, gravite, dateEmission, dateExpiration, expertId, maladieId |
| ModeleIA | ✅ | version PK, framework, precision, inputSize, dateDeployment |

### Phase 3 — Authentification JWT ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| POST /api/auth/register | ✅ 🔄 | Simplifié : nomPrenom, telephone, password (sans commune/village) |
| POST /api/auth/login | ✅ | Connexion → JWT |
| GET /api/auth/me | ✅ | Profil utilisateur connecté |
| PUT /api/auth/me | ✅ 🔄 | Simplifié : nom + telephone uniquement |
| POST /api/auth/change-password | ✅ | Changement mot de passe |

### Phase 3 bis — Parcelles agricoles ✅ 🆕
| Élément | Statut | Détail |
|---------|--------|--------|
| GET /api/parcelles/{farmer_id} | ✅ 🆕 | Lister les parcelles d'un agriculteur |
| POST /api/parcelles/{farmer_id} | ✅ 🆕 | Ajouter une parcelle |
| PUT /api/parcelles/{parcelle_id} | ✅ 🆕 | Modifier une parcelle |
| DELETE /api/parcelles/{parcelle_id} | ✅ 🆕 | Supprimer une parcelle |

### Phase 4 — Synchronisation mobile ✅ (CRITIQUE)
| Élément | Statut | Détail |
|---------|--------|--------|
| POST /sync/profile | ✅ | Sync profil agriculteur |
| POST /api/users/sync | ✅ | Alias pour compatibilité mobile |
| POST /sync/diagnostics | ✅ | Upload diagnostic |
| POST /api/diagnostics/upload | ✅ | Alias pour compatibilité mobile |
| GET /sync/check-updates | ✅ | Vérifier mises à jour |
| GET /api/updates/check | ✅ | Alias pour compatibilité mobile |
| GET /sync/download | ✅ | Télécharger bundle complet |
| GET /api/updates/download | ✅ | Alias pour compatibilité mobile |
| GET /sync/knowledge-base | ✅ | Base de connaissances |
| GET /sync/alerts | ✅ | Alertes actives |
| GET /sync/model-updates | ✅ | Info modèle IA |
| GET /sync/app-config | ✅ | Configuration app |

### Phase 5 — Base de connaissances ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| GET /api/knowledge/maladies | ✅ | Liste des maladies |
| GET /api/knowledge/maladies/{id} | ✅ | Détail maladie |
| POST /api/knowledge/maladies | ✅ | Créer maladie (admin) |
| PUT /api/knowledge/maladies/{id} | ✅ | Modifier maladie (admin) |
| DELETE /api/knowledge/maladies/{id} | ✅ | Supprimer maladie (admin) |
| GET /api/knowledge/traitements | ✅ | Liste des traitements |
| POST /api/knowledge/traitements | ✅ | Créer traitement (admin) |
| PUT /api/knowledge/traitements/{id} | ✅ | Modifier traitement (admin) |
| DELETE /api/knowledge/traitements/{id} | ✅ | Supprimer traitement (admin) |

### Phase 6 — Alertes agricoles ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| GET /api/alerts/ | ✅ | Liste alertes (filtre zone, active_only) |
| GET /api/alerts/{id} | ✅ | Détail alerte |
| POST /api/alerts/ | ✅ | Créer alerte (expert/admin) |
| PUT /api/alerts/{id} | ✅ | Modifier alerte (expert/admin) |
| DELETE /api/alerts/{id} | ✅ | Supprimer alerte (expert/admin) |
| Alertes sample | ✅ | 6 alertes d'exemple (Niger) |

### Phase 7 — Gouvernance ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| CRUD Admin | ✅ | GET/POST/PUT/DELETE protégés ADMIN |
| CRUD Expert | ✅ | POST/PUT/DELETE protégés ADMIN, GET public |
| Contrôle d'accès | ✅ | Dépendances `get_current_admin`, `get_current_expert` |

### Phase 8 — Dockerisation ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| Dockerfile | ✅ | Python 3.12-slim + PostgreSQL deps |
| docker-compose.yml | ✅ | API + PostgreSQL avec healthcheck |
| requirements.txt | ✅ | Toutes dépendances versionnées |

### Phase 9 — Documentation ✅
| Élément | Statut | Détail |
|---------|--------|--------|
| README.md | ✅ | Guide complet d'installation et utilisation |
| Swagger UI | ✅ | Auto-généré sur /docs |
| ReDoc | ✅ | Alternative sur /redoc |
| Code commenté | ✅ | Docstrings pédagogiques partout |

---

## 🔗 Cohérence backend ↔ mobile

### Endpoints attendus par le mobile

| Endpoint mobile | Backend | Statut |
|-----------------|---------|--------|
| POST /api/users/sync | /api/users/sync ET /sync/profile | ✅ |
| POST /api/diagnostics/upload | /api/diagnostics/upload ET /sync/diagnostics | ✅ |
| GET /api/updates/check | /api/updates/check ET /sync/check-updates | ✅ |
| GET /api/updates/download | /api/updates/download ET /sync/download | ✅ |

### DTOs alignés

| DTO Mobile (Kotlin) | DTO Backend (Python) | Statut |
|---------------------|---------------------|--------|
| UserSyncDTO (nomPrenom + parcelles[]) | UserSyncDTO (nomPrenom + parcelles[]) | ✅ 🔄 |
| ParcelleSyncDTO | ParcelleSyncDTO | ✅ 🆕 |
| DiagnosticUploadDTO (+ parcelleId) | DiagnosticUploadDTO (+ parcelleId) | ✅ 🔄 |
| LocationUploadDTO | LocationUploadDTO | ✅ |
| PredictionUploadDTO | PredictionUploadDTO | ✅ |
| CheckUpdatesResponseDTO | CheckUpdatesResponseDTO | ✅ |
| KnowledgeBaseDTO | KnowledgeBaseDTO | ✅ |
| MaladieDTO | MaladieDTO | ✅ |
| TraitementDTO | TraitementDTO | ✅ |
| AlerteDTO | AlerteDTO | ✅ |
| ModelUpdateDTO | ModelUpdateDTO | ✅ |
| AppConfigDTO | AppConfigDTO | ✅ |
| UpdateBundleDTO | UpdateBundleDTO | ✅ |

---

## 📡 Endpoints API complets

### Système
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | /health | Vérification santé API |
| GET | / | Page d'accueil |
| GET | /docs | Swagger UI |

### Authentification
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | /api/auth/register | Non | Créer un compte |
| POST | /api/auth/login | Non | Connexion → JWT |
| GET | /api/auth/me | Oui | Profil courant |
| PUT | /api/auth/me | Oui | Modifier profil |
| POST | /api/auth/change-password | Oui | Changer MDP |

### Synchronisation mobile
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | /sync/profile | Non | Sync profil |
| POST | /api/users/sync | Non | Alias |
| POST | /sync/diagnostics | Non | Upload diagnostic |
| POST | /api/diagnostics/upload | Non | Alias |
| GET | /sync/check-updates | Non | Vérifier MAJ |
| GET | /api/updates/check | Non | Alias |
| GET | /sync/download | Non | Bundle complet |
| GET | /api/updates/download | Non | Alias |
| GET | /sync/knowledge-base | Non | Maladies + traitements |
| GET | /sync/alerts | Non | Alertes actives |
| GET | /sync/model-updates | Non | Info modèle IA |
| GET | /sync/app-config | Non | Config app |

### Base de connaissances
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | /api/knowledge/maladies | Non | Liste maladies |
| GET | /api/knowledge/maladies/{id} | Non | Détail |
| POST | /api/knowledge/maladies | Admin | Créer |
| PUT | /api/knowledge/maladies/{id} | Admin | Modifier |
| DELETE | /api/knowledge/maladies/{id} | Admin | Supprimer |
| GET | /api/knowledge/traitements | Non | Liste |
| POST | /api/knowledge/traitements | Admin | Créer |
| PUT | /api/knowledge/traitements/{id} | Admin | Modifier |
| DELETE | /api/knowledge/traitements/{id} | Admin | Supprimer |

### Alertes
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | /api/alerts/ | Non | Liste |
| GET | /api/alerts/{id} | Non | Détail |
| POST | /api/alerts/ | Expert/Admin | Créer |
| PUT | /api/alerts/{id} | Expert/Admin | Modifier |
| DELETE | /api/alerts/{id} | Expert/Admin | Supprimer |

### Utilisateurs
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | /api/users | Oui | Liste |
| POST | /api/users/seed | Non | Initialiser BD |

### Parcelles 🆕
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | /api/parcelles/{farmer_id} | Oui | Lister parcelles d'un agriculteur |
| POST | /api/parcelles/{farmer_id} | Oui | Ajouter une parcelle |
| PUT | /api/parcelles/{parcelle_id} | Oui | Modifier une parcelle |
| DELETE | /api/parcelles/{parcelle_id} | Oui | Supprimer une parcelle |

---

## 🧪 Comment tester

### 1. Lancer la stack Docker

```powershell
cd C:\GREMAHTECH\GremahTech\PFE\AgriPredict\backend
docker compose up --build
```

### 2. Initialiser la base

```http
POST http://localhost:8000/api/users/seed
```

### 3. Tester l'authentification

```http
# Inscription (simplifié — sans commune/village)
POST http://localhost:8000/api/auth/register
Content-Type: application/json

{
  "nomPrenom": "Moussa Ibrahim",
  "telephone": "+22790123456",
  "password": "motdepasse123"
}

# Connexion
POST http://localhost:8000/api/auth/login
Content-Type: application/json

{
  "telephone": "+22790123456",
  "password": "motdepasse123"
}

# Utiliser le token dans les requêtes suivantes
GET http://localhost:8000/api/auth/me
Authorization: Bearer <token>
```

### 4. Tester la synchronisation

```http
# Sync profil + parcelles (simule mobile)
POST http://localhost:8000/api/users/sync
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nomPrenom": "Test Mobile",
  "telephone": "+22799999999",
  "parcelles": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "nomParcelle": "Champ Nord",
      "commune": "Maradi",
      "village": "Dan Issa",
      "ville": "Maradi"
    }
  ]
}

# Vérifier mises à jour
GET http://localhost:8000/api/updates/check

# Télécharger tout
GET http://localhost:8000/api/updates/download
```

---

## 🚀 Prochaines étapes

| Priorité | Tâche | Effort |
|----------|-------|--------|
| ⬜ | Tests unitaires | ★★★ |
| ⬜ | Upload image réel | ★★★ |
| ⬜ | Validation expert diagnostic | ★★ |
| ⬜ | Versioning dynamique | ★★ |
| ⬜ | Logging structuré | ★ |
| ⬜ | CI/CD | ★★ |

---

## 🏥 Santé du backend

| Critère | État |
|---------|------|
| Architecture | ✅ Simple et pédagogique |
| Modèle de données | ✅ Complet (13 entités, + Parcelle) |
| Auth JWT | ✅ Fonctionnel |
| Sync mobile | ✅ Routes compatibles |
| Base connaissances | ✅ CRUD + seeder |
| Alertes | ✅ CRUD complet |
| Dockerisation | ✅ Prêt |
| Documentation | ✅ README + Swagger |
| Tests | ⚠️ Minimal |

### Score global : 🟢 9/10

---

## 📈 Statistiques

| Métrique | Valeur |
|----------|--------|
| Entités SQLAlchemy | 13 (+ Parcelle) |
| Endpoints API | 40+ |
| Fichiers Python | ~35 |
| Maladies pré-chargées | 24 |
| Traitements pré-chargés | 41 |
| Alertes d'exemple | 6 |
| Lignes de code estimées | ~3000 |

---

> 📌 **Backend prêt pour la soutenance !**
> Documentation Swagger : http://localhost:8000/docs
