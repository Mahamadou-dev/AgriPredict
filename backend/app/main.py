"""
Point d'entrée principal de l'API AgriPredict.

Cette application FastAPI expose les endpoints pour:
- Authentification des utilisateurs (JWT)
- Synchronisation mobile ↔ serveur
- Gestion des diagnostics
- Base de connaissances (maladies/traitements)
- Alertes agricoles
- Administration (experts, admins)

Architecture pédagogique pour projet de fin d'études.
"""

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.api.routes import admins, alerts, auth, diagnostics, experts, knowledge, parcelles, sync, users
from app.core.config import settings

# Création de l'application FastAPI
app = FastAPI(
    title=settings.APP_NAME,
    version="1.0.0",
    description="""
## 🌾 AgriPredict API

Backend pour l'application mobile AgriPredict.

### Fonctionnalités:
- **Authentification** : Inscription, connexion, gestion de profil (JWT)
- **Synchronisation** : Envoi/réception de données mobile ↔ serveur
- **Diagnostics** : Réception des analyses IA depuis les mobiles
- **Base de connaissances** : Maladies et traitements recommandés
- **Alertes** : Alertes agricoles régionales
- **Administration** : Gestion des experts et administrateurs

### Projet de fin d'études
Licence en Génie Logiciel
    """,
    docs_url="/docs",      # Swagger UI
    redoc_url="/redoc",    # ReDoc alternative
)

# ============================================================================
# MIDDLEWARES
# ============================================================================

# CORS : permet les requêtes cross-origin (nécessaire pour le mobile)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],       # En production, limiter aux origines autorisées
    allow_credentials=True,
    allow_methods=["*"],       # GET, POST, PUT, DELETE, etc.
    allow_headers=["*"],       # Authorization, Content-Type, etc.
)


# ============================================================================
# GESTIONNAIRES D'ERREURS GLOBAUX
# ============================================================================

@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    """
    Capture les ValueError et retourne une erreur 400 (Bad Request).
    
    Utile pour les erreurs de validation métier (ex: "Agriculteur introuvable").
    """
    return JSONResponse(
        status_code=400,
        content={"detail": str(exc)},
    )


@app.exception_handler(Exception)
async def generic_error_handler(request: Request, exc: Exception):
    """
    Capture toutes les autres exceptions et retourne une erreur 500.
    
    En mode debug, inclut le message d'erreur pour le débogage.
    """
    if settings.DEBUG:
        return JSONResponse(
            status_code=500,
            content={"detail": f"Erreur interne: {str(exc)}"},
        )
    return JSONResponse(
        status_code=500,
        content={"detail": "Erreur interne du serveur"},
    )


# ============================================================================
# ROUTES SYSTÈME
# ============================================================================

@app.get("/health", tags=["système"])
def health_check():
    """
    Vérifie que l'API est en ligne.
    
    Utilisé par Docker, les load balancers, et le monitoring.
    """
    return {"status": "ok", "service": "AgriPredict API"}


@app.get("/", tags=["système"])
def root():
    """
    Page d'accueil de l'API.
    """
    return {
        "message": "Bienvenue sur l'API AgriPredict",
        "documentation": "/docs",
        "version": "1.0.0",
    }


# ============================================================================
# INCLUSION DES ROUTERS
# ============================================================================

# Routes d'authentification (/api/auth/*)
app.include_router(auth.router, prefix="/api", tags=["authentification"])

# Routes utilisateurs (/api/users/*)
app.include_router(users.router, prefix="/api", tags=["utilisateurs"])

# Routes diagnostics (/api/diagnostics/*)
app.include_router(diagnostics.router, prefix="/api", tags=["diagnostics"])

# Routes base de connaissances (/api/knowledge/*)
app.include_router(knowledge.router, prefix="/api", tags=["base de connaissances"])

# Routes alertes (/api/alerts/*)
app.include_router(alerts.router, prefix="/api", tags=["alertes"])

# Routes parcelles (/api/parcelles/*)
app.include_router(parcelles.router, prefix="/api", tags=["parcelles"])

# Routes experts (/api/experts/*)
app.include_router(experts.router, prefix="/api", tags=["experts"])

# Routes admins (/api/admins/*)
app.include_router(admins.router, prefix="/api", tags=["administration"])

# Routes synchronisation (/sync/* et /api/*) - SANS préfixe car contient déjà /sync et /api
app.include_router(sync.router, tags=["synchronisation"])
