"""
Configuration centralisée de l'application.

Utilise pydantic-settings pour charger les variables d'environnement.
Simple et explicable devant un jury académique.
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    Configuration de l'application AgriPredict.
    
    Les valeurs peuvent être surchargées via un fichier .env
    ou des variables d'environnement.
    """
    
    # Nom de l'application
    APP_NAME: str = "AgriPredict API"
    
    # URL de connexion PostgreSQL (format: postgresql+psycopg://user:password@host:port/dbname)
    DATABASE_URL: str = "postgresql+psycopg://agripredict:agripredict@db:5432/agripredict"
    
    # Clé secrète pour signer les tokens JWT (À CHANGER EN PRODUCTION!)
    SECRET_KEY: str = "change-me-in-production-avec-une-cle-longue-et-aleatoire"
    
    # Durée de validité des tokens JWT en minutes (24 heures par défaut)
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24
    
    # Algorithme de signature JWT
    ALGORITHM: str = "HS256"
    
    # Mode debug (affiche plus de détails en cas d'erreur)
    DEBUG: bool = False

    model_config = SettingsConfigDict(
        env_file=".env", 
        env_file_encoding="utf-8", 
        extra="ignore"
    )


# Instance unique de la configuration (singleton pattern simple)
settings = Settings()
