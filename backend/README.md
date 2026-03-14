# 🌾 AgriPredict Backend

> Backend API pour l'application mobile AgriPredict
> Projet de fin d'études — Licence en Génie Logiciel

---

## 📋 Table des matières

1. [Présentation](#-présentation)
2. [Stack technique](#%EF%B8%8F-stack-technique)
3. [Architecture](#%EF%B8%8F-architecture)
4. [Installation](#-installation)
5. [Configuration](#-configuration)
6. [Endpoints API](#-endpoints-api)
7. [Base de données](#%EF%B8%8F-base-de-données)
8. [Tests](#-tests)
9. [Déploiement](#-déploiement)

---

## 🎯 Présentation

Le backend AgriPredict est une API REST qui sert à :

- **Centraliser** les données des agriculteurs
- **Synchroniser** les diagnostics depuis les mobiles (offline-first)
- **Distribuer** la base de connaissances (maladies/traitements)
- **Publier** des alertes agricoles régionales
- **Gérer** les utilisateurs (agriculteurs, experts, admins)

### Principes

- ✅ Architecture simple et pédagogique
- ✅ Code commenté et explicable
- ✅ Compatible avec l'application mobile Android
- ✅ Prêt pour Docker

---

## ⚙️ Stack technique

| Composant | Technologie | Version |
|-----------|-------------|---------|
| Langage | Python | 3.12 |
| Framework API | FastAPI | 0.115.8 |
| ORM | SQLAlchemy | 2.0.38 |
| Validation | Pydantic | 2.10.6 |
| Base de données | PostgreSQL | 16 |
| Migrations | Alembic | 1.14.1 |
| Auth | JWT (python-jose) | 3.3.0 |
| Hash mots de passe | bcrypt | 4.2.1 |
| Serveur | Uvicorn | 0.35.0 |
| Conteneurs | Docker + Compose | — |

---

## 🏗️ Architecture

```
backend/
├── app/
│   ├── main.py              # Point d'entrée FastAPI
│   ├── core/
│   │   ├── config.py        # Configuration (.env)
│   │   ├── database.py      # Connexion PostgreSQL
│   │   └── security.py      # JWT + hash passwords
│   ├── models/
│   │   ├── base.py          # Base SQLAlchemy
│   │   └── entities.py      # Entités (User, Diagnostic, etc.)
│   ├── schemas/
│   │   ├── auth.py          # DTOs authentification
│   │   ├── common.py        # DTOs partagés
│   │   ├── sync.py          # DTOs synchronisation
│   │   └── management.py    # DTOs admin/expert
│   ├── crud/
│   │   ├── auth.py          # CRUD utilisateurs
│   │   ├── diagnostics.py   # CRUD diagnostics
│   │   ├── knowledge.py     # CRUD maladies/traitements
│   │   ├── alerts.py        # CRUD alertes
│   │   └── ...
│   ├── api/
│   │   ├── deps.py          # Dépendances (auth, rôles)
│   │   └── routes/
│   │       ├── auth.py      # /api/auth/*
│   │       ├── sync.py      # /sync/* et /api/*
│   │       ├── knowledge.py # /api/knowledge/*
│   │       ├── alerts.py    # /api/alerts/*
│   │       └── ...
│   └── services/
│       └── seeder.py        # Données initiales
├── alembic/                 # Migrations DB
├── tests/                   # Tests pytest
├── Dockerfile
├── docker-compose.yml
└── requirements.txt
```

---

## 🚀 Installation

### Option 1 : Docker (recommandé)

```bash
# Cloner le projet
cd backend

# Lancer PostgreSQL + API
docker compose up --build

# L'API est accessible sur http://localhost:8000
# La documentation Swagger sur http://localhost:8000/docs
```

### Option 2 : Installation locale

```bash
# Créer un environnement virtuel
python -m venv venv
venv\Scripts\activate  # Windows
source venv/bin/activate  # Linux/Mac

# Installer les dépendances
pip install -r requirements.txt

# Configurer la base de données (voir Configuration)

# Appliquer les migrations
alembic upgrade head

# Lancer le serveur
uvicorn app.main:app --reload
```

---

## ⚙️ Configuration

Créez un fichier `.env` à la racine du dossier `backend/` :

```env
# Base de données
DATABASE_URL=postgresql+psycopg://agripredict:agripredict@localhost:5432/agripredict

# Sécurité JWT (à changer en production!)
SECRET_KEY=votre-cle-secrete-longue-et-aleatoire
ACCESS_TOKEN_EXPIRE_MINUTES=1440

# Mode debug
DEBUG=true
```

---

## 📡 Endpoints API

### Authentification (`/api/auth`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/register` | Créer un compte agriculteur |
| POST | `/api/auth/login` | Se connecter (retourne un JWT) |
| GET | `/api/auth/me` | Profil de l'utilisateur connecté |
| PUT | `/api/auth/me` | Mettre à jour son profil |
| POST | `/api/auth/change-password` | Changer son mot de passe |

### Synchronisation mobile (`/sync` et `/api`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/sync/profile` ou `/api/users/sync` | Sync profil agriculteur |
| POST | `/sync/diagnostics` ou `/api/diagnostics/upload` | Upload diagnostic |
| GET | `/sync/check-updates` ou `/api/updates/check` | Vérifier mises à jour |
| GET | `/sync/download` ou `/api/updates/download` | Télécharger bundle |
| GET | `/sync/knowledge-base` | Base de connaissances |
| GET | `/sync/alerts` | Alertes actives |
| GET | `/sync/model-updates` | Info modèle IA |
| GET | `/sync/app-config` | Configuration app |

### Base de connaissances (`/api/knowledge`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/knowledge/maladies` | Liste des maladies |
| GET | `/api/knowledge/maladies/{id}` | Détail d'une maladie |
| POST | `/api/knowledge/maladies` | Créer (admin) |
| PUT | `/api/knowledge/maladies/{id}` | Modifier (admin) |
| DELETE | `/api/knowledge/maladies/{id}` | Supprimer (admin) |
| GET | `/api/knowledge/traitements` | Liste des traitements |
| ... | ... | ... |

### Alertes (`/api/alerts`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/alerts/` | Liste des alertes |
| POST | `/api/alerts/` | Créer (expert/admin) |
| PUT | `/api/alerts/{id}` | Modifier (expert/admin) |
| DELETE | `/api/alerts/{id}` | Supprimer (expert/admin) |

### Système

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/health` | Vérification santé API |
| GET | `/docs` | Documentation Swagger |
| GET | `/redoc` | Documentation ReDoc |

---

## 🗄️ Base de données

### Modèle de données

```
┌─────────────┐     ┌──────────────┐
│    User     │────<│    Farmer    │
│─────────────│     │──────────────│
│ id (UUID)   │     │ commune      │
│ nom         │     │ village      │
│ telephone   │     └──────────────┘
│ email       │
│ role        │────<┌──────────────┐
│ passwordHash│     │    Expert    │
└─────────────┘     │──────────────│
       │            │ specialite   │
       │            │ matricule    │
       └───────────<└──────────────┘

┌─────────────┐     ┌──────────────┐
│  Diagnostic │────>│   Farmer     │
│─────────────│     └──────────────┘
│ id (UUID)   │
│ date        │────>┌──────────────┐
│ locationId  │     │   Location   │
│ predictionId│     └──────────────┘
└─────────────┘
       │
       └───────────>┌──────────────┐
                    │  Prediction  │
                    │──────────────│
                    │ label        │
                    │ confidence   │
                    │ maladieId    │──>┌──────────────┐
                    └──────────────┘   │   Maladie    │
                                       │──────────────│
┌─────────────┐                        │ nomCommun    │
│   Alert     │───────────────────────>│ description  │
│─────────────│                        └──────────────┘
│ message     │                               │
│ zone        │                               │
│ gravite     │                               ▼
│ expertId    │──>Expert              ┌──────────────┐
└─────────────┘                       │  Traitement  │
                                      │──────────────│
                                      │ titre        │
                                      │ dosage       │
                                      └──────────────┘
```

### Migrations

```bash
# Créer une nouvelle migration
alembic revision --autogenerate -m "description"

# Appliquer les migrations
alembic upgrade head

# Revenir en arrière
alembic downgrade -1
```

---

## 🧪 Tests

```bash
# Lancer tous les tests
pytest

# Avec couverture
pytest --cov=app

# Tests spécifiques
pytest tests/test_health.py
```

---

## 🚀 Déploiement

### Docker Compose (développement/production simple)

```bash
# Build et lancement
docker compose up --build -d

# Voir les logs
docker compose logs -f api

# Arrêter
docker compose down
```

### Variables d'environnement de production

```env
DATABASE_URL=postgresql+psycopg://user:password@host:5432/agripredict
SECRET_KEY=une-cle-tres-longue-et-aleatoire-generee
DEBUG=false
```

---

## 📈 Statistiques

| Métrique | Valeur |
|----------|--------|
| Entités SQLAlchemy | 12 |
| Endpoints API | 30+ |
| Fichiers Python | ~25 |
| Maladies pré-chargées | 24 |
| Traitements pré-chargés | 41 |

---

## 👨‍🎓 Notes pour la soutenance

- **FastAPI** : Framework moderne, performant, avec documentation auto-générée
- **SQLAlchemy 2.0** : ORM Python standard, syntaxe type-annotée
- **Pydantic v2** : Validation des données avec messages d'erreur clairs
- **JWT** : Standard industrie pour l'authentification stateless
- **Architecture simple** : Facile à expliquer et maintenir
- **Docker** : Déploiement reproductible et portable

---

> 📌 Documentation interactive : http://localhost:8000/docs
