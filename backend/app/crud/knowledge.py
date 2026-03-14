"""
CRUD pour la base de connaissances (maladies et traitements).

Gestion du référentiel des maladies et leurs traitements recommandés.
"""

from sqlalchemy.orm import Session

from app.models.entities import Maladie, Traitement


# ============================================================================
# MALADIES
# ============================================================================

def list_maladies(db: Session) -> list[Maladie]:
    """
    Liste toutes les maladies.
    """
    return db.query(Maladie).order_by(Maladie.id).all()


def get_maladie_by_id(db: Session, maladie_id: int) -> Maladie | None:
    """
    Récupère une maladie par son ID.
    """
    return db.query(Maladie).filter(Maladie.id == maladie_id).first()


def create_maladie(
    db: Session,
    id: int,
    nom_commun: str,
    nom_scientifique: str = "",
    description: str = "",
) -> Maladie:
    """
    Crée une nouvelle maladie.
    
    L'ID est fixe car il doit correspondre aux classes du modèle IA.
    """
    maladie = Maladie(
        id=id,
        nom_commun=nom_commun,
        nom_scientifique=nom_scientifique,
        description=description,
    )
    db.add(maladie)
    db.commit()
    db.refresh(maladie)
    return maladie


def update_maladie(
    db: Session,
    maladie_id: int,
    nom_commun: str | None = None,
    nom_scientifique: str | None = None,
    description: str | None = None,
) -> Maladie | None:
    """
    Met à jour une maladie existante.
    """
    maladie = get_maladie_by_id(db, maladie_id)
    if not maladie:
        return None
    
    if nom_commun is not None:
        maladie.nom_commun = nom_commun
    if nom_scientifique is not None:
        maladie.nom_scientifique = nom_scientifique
    if description is not None:
        maladie.description = description
    
    db.commit()
    db.refresh(maladie)
    return maladie


def delete_maladie(db: Session, maladie_id: int) -> bool:
    """
    Supprime une maladie et ses traitements associés (CASCADE).
    """
    maladie = get_maladie_by_id(db, maladie_id)
    if maladie:
        db.delete(maladie)
        db.commit()
        return True
    return False


# ============================================================================
# TRAITEMENTS
# ============================================================================

def list_traitements(db: Session, maladie_id: int | None = None) -> list[Traitement]:
    """
    Liste les traitements, optionnellement filtrés par maladie.
    """
    query = db.query(Traitement)
    if maladie_id is not None:
        query = query.filter(Traitement.maladie_id == maladie_id)
    return query.order_by(Traitement.id).all()


def get_traitement_by_id(db: Session, traitement_id: int) -> Traitement | None:
    """
    Récupère un traitement par son ID.
    """
    return db.query(Traitement).filter(Traitement.id == traitement_id).first()


def create_traitement(
    db: Session,
    id: int,
    titre: str,
    maladie_id: int,
    description: str = "",
    dosage: str = "",
) -> Traitement:
    """
    Crée un nouveau traitement.
    """
    traitement = Traitement(
        id=id,
        titre=titre,
        description=description,
        dosage=dosage,
        maladie_id=maladie_id,
    )
    db.add(traitement)
    db.commit()
    db.refresh(traitement)
    return traitement


def update_traitement(
    db: Session,
    traitement_id: int,
    titre: str | None = None,
    description: str | None = None,
    dosage: str | None = None,
) -> Traitement | None:
    """
    Met à jour un traitement existant.
    """
    traitement = get_traitement_by_id(db, traitement_id)
    if not traitement:
        return None
    
    if titre is not None:
        traitement.titre = titre
    if description is not None:
        traitement.description = description
    if dosage is not None:
        traitement.dosage = dosage
    
    db.commit()
    db.refresh(traitement)
    return traitement


def delete_traitement(db: Session, traitement_id: int) -> bool:
    """
    Supprime un traitement.
    """
    traitement = get_traitement_by_id(db, traitement_id)
    if traitement:
        db.delete(traitement)
        db.commit()
        return True
    return False
