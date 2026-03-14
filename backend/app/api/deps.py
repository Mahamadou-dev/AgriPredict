"""
Dépendances partagées pour les routes API.

Contient les dépendances d'injection pour:
- Authentification (get_current_user)
- Contrôle d'accès par rôle (require_roles)
"""

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.core.security import decode_access_token
from app.crud.auth import get_user_by_phone
from app.models.entities import User, UserRole

# Configuration OAuth2
# Le tokenUrl indique l'endpoint pour obtenir un token (pour Swagger UI)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")


def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db),
) -> User:
    """
    Extrait l'utilisateur connecté depuis le token JWT.

    Cette dépendance est utilisée pour protéger les routes qui
    nécessitent une authentification.

    Usage:
        @router.get("/protected")
        def protected_route(user: User = Depends(get_current_user)):
            return {"user": user.nom}

    Raises:
        HTTPException 401: Si le token est invalide ou expiré
    """
    # Décoder le token pour extraire le téléphone
    telephone = decode_access_token(token)
    if not telephone:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalide ou expiré",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Récupérer l'utilisateur depuis la base
    user = get_user_by_phone(db, telephone)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Utilisateur introuvable",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Vérifier que le compte est actif
    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Compte désactivé",
        )

    return user


def require_roles(*roles: UserRole):
    """
    Crée une dépendance qui vérifie que l'utilisateur a un des rôles requis.

    Usage:
        @router.post("/admin-only")
        def admin_route(user: User = Depends(require_roles(UserRole.ADMIN))):
            return {"message": "Bienvenue admin"}

        @router.post("/staff")
        def staff_route(user: User = Depends(require_roles(UserRole.ADMIN, UserRole.EXPERT))):
            return {"message": "Bienvenue"}

    Args:
        *roles: Les rôles autorisés pour cette route

    Returns:
        Une dépendance FastAPI qui vérifie le rôle

    Raises:
        HTTPException 403: Si l'utilisateur n'a pas le rôle requis
    """
    def dependency(user: User = Depends(get_current_user)) -> User:
        if user.role not in roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Permissions insuffisantes. Rôle requis: " + ", ".join(
                    r.value for r in roles
                ),
            )
        return user

    return dependency


# Raccourcis pratiques pour les rôles courants
def get_current_admin(user: User = Depends(require_roles(UserRole.ADMIN))) -> User:
    """Dépendance pour les routes admin uniquement."""
    return user


def get_current_expert(
    user: User = Depends(require_roles(UserRole.EXPERT, UserRole.ADMIN))
) -> User:
    """Dépendance pour les routes expert ou admin."""
    return user


def get_current_farmer(user: User = Depends(get_current_user)) -> User:
    """Dépendance pour tout utilisateur connecté (agriculteur inclus)."""
    return user
