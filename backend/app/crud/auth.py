"""
CRUD pour l'authentification.

Fonctions pour créer, rechercher et authentifier les utilisateurs.
"""

import uuid

from sqlalchemy.orm import Session

from app.core.security import get_password_hash, verify_password
from app.models.entities import Admin, Expert, Farmer, User, UserRole


def get_user_by_phone(db: Session, telephone: str) -> User | None:
    """
    Recherche un utilisateur par son numéro de téléphone.

    Args:
        db: Session de base de données
        telephone: Numéro de téléphone à rechercher

    Returns:
        L'utilisateur trouvé ou None
    """
    return db.query(User).filter(User.telephone == telephone).first()


def get_user_by_id(db: Session, user_id: str | uuid.UUID) -> User | None:
    """
    Recherche un utilisateur par son ID.
    """
    if isinstance(user_id, str):
        user_id = uuid.UUID(user_id)
    return db.query(User).filter(User.id == user_id).first()


def authenticate(db: Session, telephone: str, password: str) -> User | None:
    """
    Authentifie un utilisateur avec son téléphone et mot de passe.

    Args:
        db: Session de base de données
        telephone: Numéro de téléphone
        password: Mot de passe en clair

    Returns:
        L'utilisateur si authentifié, None sinon
    """
    user = get_user_by_phone(db, telephone)
    if not user:
        return None
    if not verify_password(password, user.password_hash):
        return None
    if not user.is_active:
        return None
    return user


def create_farmer_user(
    db: Session,
    nom: str,
    telephone: str,
    password: str,
    email: str = "",
) -> User:
    """
    Crée un nouveau compte agriculteur.

    Cette fonction crée à la fois l'entrée User et l'entrée Farmer associée.
    Les parcelles (commune, village, ville) sont ajoutées séparément.

    Args:
        db: Session de base de données
        nom: Nom et prénom
        telephone: Numéro de téléphone (unique)
        password: Mot de passe en clair (sera haché)
        email: Email optionnel

    Returns:
        Le nouvel utilisateur créé
    """
    user_id = uuid.uuid4()

    # Créer l'utilisateur de base
    user = User(
        id=user_id,
        nom=nom,
        telephone=telephone,
        email=email,
        password_hash=get_password_hash(password),
        role=UserRole.AGRICULTEUR,
        is_active=True,
    )
    db.add(user)
    db.flush()  # Assure que l'user existe avant de créer le farmer

    # Créer le profil agriculteur associé (sans localisation)
    farmer = Farmer(id=user_id)
    db.add(farmer)
    db.commit()
    db.refresh(user)

    return user


def create_expert_user(
    db: Session,
    nom: str,
    telephone: str,
    password: str,
    matricule: str,
    email: str = "",
    specialite: str = "",
    commune: str = "",
) -> User:
    """
    Crée un nouveau compte expert.
    """
    user_id = uuid.uuid4()

    user = User(
        id=user_id,
        nom=nom,
        telephone=telephone,
        email=email,
        password_hash=get_password_hash(password),
        role=UserRole.EXPERT,
        is_active=True,
    )
    db.add(user)
    db.flush()

    expert = Expert(
        id=user_id,
        specialite=specialite,
        matricule=matricule,
        commune=commune,
    )
    db.add(expert)
    db.commit()
    db.refresh(user)

    return user


def create_admin_user(
    db: Session,
    nom: str,
    telephone: str,
    password: str,
    email: str = "",
    niveau_acces: int = 1,
) -> User:
    """
    Crée un nouveau compte administrateur.
    """
    user_id = uuid.uuid4()

    user = User(
        id=user_id,
        nom=nom,
        telephone=telephone,
        email=email,
        password_hash=get_password_hash(password),
        role=UserRole.ADMIN,
        is_active=True,
    )
    db.add(user)
    db.flush()

    admin = Admin(
        id=user_id,
        niveau_acces=niveau_acces,
    )
    db.add(admin)
    db.commit()
    db.refresh(user)

    return user
