"""
Configuration de la connexion à la base de données.

Utilise SQLAlchemy 2.0 avec PostgreSQL.
Le pattern "dependency injection" de FastAPI est utilisé pour
injecter la session DB dans les routes.
"""

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session

from app.core.config import settings

# Création du moteur de connexion PostgreSQL
# pool_pre_ping=True permet de vérifier que la connexion est valide avant de l'utiliser
engine = create_engine(
    settings.DATABASE_URL, 
    pool_pre_ping=True,
    echo=settings.DEBUG  # Affiche les requêtes SQL si DEBUG=True
)

# Factory de sessions - crée de nouvelles sessions pour chaque requête
SessionLocal = sessionmaker(
    autocommit=False,  # Pas de commit automatique
    autoflush=False,   # Pas de flush automatique
    bind=engine
)


def get_db() -> Session:
    """
    Générateur de session DB pour l'injection de dépendances FastAPI.
    
    Utilisation dans une route:
        @router.get("/items")
        def get_items(db: Session = Depends(get_db)):
            return db.query(Item).all()
    
    La session est automatiquement fermée après la requête.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
