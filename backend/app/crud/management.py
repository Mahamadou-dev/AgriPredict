"""
CRUD pour la gestion des profils (admin, expert, utilisateurs).

Fonctions de gestion des comptes et profils.
"""

import uuid

from sqlalchemy.orm import Session

from app.core.security import get_password_hash, verify_password
from app.crud.auth import create_admin_user, create_expert_user
from app.models.entities import Admin, Expert, Farmer, User


# ============================================================================
# GESTION DU PROFIL UTILISATEUR
# ============================================================================

def update_farmer_profile(
    db: Session,
    user: User,
    nom: str,
    telephone: str,
) -> User:
    """
    Met à jour le profil d'un agriculteur (nom et téléphone uniquement).
    Les parcelles sont gérées séparément via les routes /parcelles.
    """
    user.nom = nom
    user.telephone = telephone

    db.commit()
    db.refresh(user)
    return user


def change_password(
    db: Session,
    user: User,
    old_password: str,
    new_password: str,
) -> bool:
    """
    Change le mot de passe d'un utilisateur.
    
    Returns:
        True si le changement a réussi, False si l'ancien mot de passe est incorrect
    """
    if not verify_password(old_password, user.password_hash):
        return False
    
    user.password_hash = get_password_hash(new_password)
    db.commit()
    return True


# ============================================================================
# GESTION DES EXPERTS
# ============================================================================

def list_experts(db: Session) -> list[Expert]:
    """
    Liste tous les experts.
    """
    return db.query(Expert).all()


def get_expert_by_id(db: Session, expert_id: str | uuid.UUID) -> Expert | None:
    """
    Récupère un expert par son ID.
    """
    if isinstance(expert_id, str):
        expert_id = uuid.UUID(expert_id)
    return db.query(Expert).filter(Expert.id == expert_id).first()


def update_expert(
    db: Session,
    expert_id: str | uuid.UUID,
    nom: str | None = None,
    telephone: str | None = None,
    email: str | None = None,
    specialite: str | None = None,
    commune: str | None = None,
) -> Expert | None:
    """
    Met à jour un expert existant.
    """
    expert = get_expert_by_id(db, expert_id)
    if not expert:
        return None
    
    if nom is not None:
        expert.user.nom = nom
    if telephone is not None:
        expert.user.telephone = telephone
    if email is not None:
        expert.user.email = email
    if specialite is not None:
        expert.specialite = specialite
    if commune is not None:
        expert.commune = commune
    
    db.commit()
    db.refresh(expert)
    return expert


def delete_expert(db: Session, expert_id: str | uuid.UUID) -> bool:
    """
    Supprime un expert (et son compte utilisateur associé).
    """
    expert = get_expert_by_id(db, expert_id)
    if not expert:
        return False
    
    # Supprimer l'utilisateur (CASCADE supprimera l'expert)
    db.delete(expert.user)
    db.commit()
    return True


# ============================================================================
# GESTION DES ADMINS
# ============================================================================

def list_admins(db: Session) -> list[Admin]:
    """
    Liste tous les administrateurs.
    """
    return db.query(Admin).all()


def get_admin_by_id(db: Session, admin_id: str | uuid.UUID) -> Admin | None:
    """
    Récupère un admin par son ID.
    """
    if isinstance(admin_id, str):
        admin_id = uuid.UUID(admin_id)
    return db.query(Admin).filter(Admin.id == admin_id).first()


def update_admin(
    db: Session,
    admin_id: str | uuid.UUID,
    nom: str | None = None,
    telephone: str | None = None,
    email: str | None = None,
    niveau_acces: int | None = None,
) -> Admin | None:
    """
    Met à jour un administrateur existant.
    """
    admin = get_admin_by_id(db, admin_id)
    if not admin:
        return None
    
    if nom is not None:
        admin.user.nom = nom
    if telephone is not None:
        admin.user.telephone = telephone
    if email is not None:
        admin.user.email = email
    if niveau_acces is not None:
        admin.niveau_acces = niveau_acces
    
    db.commit()
    db.refresh(admin)
    return admin


def delete_admin(db: Session, admin_id: str | uuid.UUID) -> bool:
    """
    Supprime un administrateur.
    """
    admin = get_admin_by_id(db, admin_id)
    if not admin:
        return False
    
    db.delete(admin.user)
    db.commit()
    return True
