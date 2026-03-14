"""
Routes pour les utilisateurs.

Gestion des listes d'utilisateurs et initialisation de la base.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_admin, get_current_user
from app.core.database import get_db
from app.crud.users import list_users
from app.models.entities import User
from app.schemas.common import UserPublic
from app.services.seeder import seed_database, seed_sample_alerts

router = APIRouter(prefix="/users")


@router.get("", response_model=list[UserPublic])
def get_users(
    db: Session = Depends(get_db),
    _user: User = Depends(get_current_user),
):
    """
    Liste tous les utilisateurs (authentification requise).
    """
    users = list_users(db)
    return [
        {
            "id": u.id,
            "nom": u.nom,
            "telephone": u.telephone,
            "email": u.email,
            "role": u.role.value,
            "isActive": u.is_active,
            "createdAt": u.created_at,
        }
        for u in users
    ]


@router.post("/seed")
def seed_data(db: Session = Depends(get_db)):
    """
    Initialise la base de données avec les maladies et traitements.

    Cette route est publique pour permettre l'initialisation facile.
    En production, la protéger ou la supprimer.
    """
    stats = seed_database(db)
    alert_count = seed_sample_alerts(db)
    stats["alertes"] = alert_count
    return {
        "message": "Base de données initialisée",
        "stats": stats,
    }
