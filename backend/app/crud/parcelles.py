"""
CRUD pour les parcelles agricoles.

Gestion des parcelles appartenant aux agriculteurs.
Chaque agriculteur peut posséder plusieurs parcelles.
"""

import uuid

from sqlalchemy.orm import Session

from app.models.entities import Parcelle


def create_parcelle(
    db: Session,
    nom_parcelle: str,
    agriculteur_id: str | uuid.UUID,
    commune: str = "",
    village: str = "",
    ville: str = "",
    parcelle_id: str | uuid.UUID | None = None,
) -> Parcelle:
    """
    Crée une nouvelle parcelle pour un agriculteur.

    Args:
        db: Session de base de données
        nom_parcelle: Nom de la parcelle
        agriculteur_id: UUID de l'agriculteur propriétaire
        commune: Commune de la parcelle
        village: Village de la parcelle
        ville: Ville la plus proche
        parcelle_id: UUID optionnel (pour sync mobile)

    Returns:
        La parcelle créée
    """
    if isinstance(agriculteur_id, str):
        agriculteur_id = uuid.UUID(agriculteur_id)

    pid = uuid.UUID(parcelle_id) if isinstance(parcelle_id, str) else (parcelle_id or uuid.uuid4())

    parcelle = Parcelle(
        id=pid,
        nom_parcelle=nom_parcelle,
        commune=commune,
        village=village,
        ville=ville,
        agriculteur_id=agriculteur_id,
    )
    db.merge(parcelle)
    db.commit()
    db.refresh(parcelle)
    return parcelle


def get_parcelles_by_farmer(db: Session, agriculteur_id: str | uuid.UUID) -> list[Parcelle]:
    """
    Récupère toutes les parcelles d'un agriculteur.
    """
    if isinstance(agriculteur_id, str):
        agriculteur_id = uuid.UUID(agriculteur_id)
    return db.query(Parcelle).filter(Parcelle.agriculteur_id == agriculteur_id).all()


def get_parcelle_by_id(db: Session, parcelle_id: str | uuid.UUID) -> Parcelle | None:
    """
    Récupère une parcelle par son ID.
    """
    if isinstance(parcelle_id, str):
        parcelle_id = uuid.UUID(parcelle_id)
    return db.query(Parcelle).filter(Parcelle.id == parcelle_id).first()


def update_parcelle(
    db: Session,
    parcelle_id: str | uuid.UUID,
    nom_parcelle: str | None = None,
    commune: str | None = None,
    village: str | None = None,
    ville: str | None = None,
) -> Parcelle | None:
    """
    Met à jour une parcelle existante.
    """
    parcelle = get_parcelle_by_id(db, parcelle_id)
    if not parcelle:
        return None

    if nom_parcelle is not None:
        parcelle.nom_parcelle = nom_parcelle
    if commune is not None:
        parcelle.commune = commune
    if village is not None:
        parcelle.village = village
    if ville is not None:
        parcelle.ville = ville

    db.commit()
    db.refresh(parcelle)
    return parcelle


def delete_parcelle(db: Session, parcelle_id: str | uuid.UUID) -> bool:
    """
    Supprime une parcelle.

    Returns:
        True si la parcelle a été supprimée, False si introuvable
    """
    parcelle = get_parcelle_by_id(db, parcelle_id)
    if not parcelle:
        return False
    db.delete(parcelle)
    db.commit()
    return True

