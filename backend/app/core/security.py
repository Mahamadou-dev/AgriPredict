"""
Module de sécurité : authentification JWT et hachage des mots de passe.

JWT (JSON Web Token) est un standard pour transmettre des informations
de manière sécurisée entre le client et le serveur.

Le mot de passe est haché avec bcrypt (algorithme sécurisé et lent,
résistant aux attaques par force brute).
"""

from datetime import datetime, timedelta, timezone
from typing import Any

from jose import JWTError, jwt
from passlib.context import CryptContext

from app.core.config import settings

# Configuration du contexte de hachage avec bcrypt
# deprecated="auto" : gère automatiquement les anciens schémas de hachage
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Vérifie qu'un mot de passe en clair correspond à son hash.

    Args:
        plain_password: Le mot de passe saisi par l'utilisateur
        hashed_password: Le hash stocké en base de données

    Returns:
        True si le mot de passe est correct, False sinon
    """
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """
    Génère le hash bcrypt d'un mot de passe.

    Args:
        password: Le mot de passe en clair

    Returns:
        Le hash du mot de passe (stocké en DB)
    """
    return pwd_context.hash(password)


def create_access_token(subject: str, expires_delta: timedelta | None = None) -> str:
    """
    Crée un token JWT pour authentifier l'utilisateur.

    Args:
        subject: L'identifiant de l'utilisateur (téléphone dans notre cas)
        expires_delta: Durée de validité optionnelle

    Returns:
        Le token JWT signé

    Le token contient:
        - "sub" (subject): identifiant de l'utilisateur
        - "exp" (expiration): date d'expiration du token
    """
    expire = datetime.now(timezone.utc) + (
        expires_delta or timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    )
    to_encode: dict[str, Any] = {"sub": subject, "exp": expire}
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)


def decode_access_token(token: str) -> str | None:
    """
    Décode et valide un token JWT.

    Args:
        token: Le token JWT à décoder

    Returns:
        Le sujet (téléphone) si le token est valide, None sinon

    Le token est invalide si:
        - Il a expiré
        - La signature ne correspond pas
        - Le format est incorrect
    """
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        return payload.get("sub")
    except JWTError:
        return None
