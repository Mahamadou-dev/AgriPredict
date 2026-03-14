"""
Routes pour la base de connaissances (maladies et traitements).

Ces endpoints permettent de consulter et gérer le référentiel
des maladies et leurs traitements.
"""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_admin
from app.core.database import get_db
from app.crud.knowledge import (
    create_maladie,
    create_traitement,
    delete_maladie,
    delete_traitement,
    get_maladie_by_id,
    get_traitement_by_id,
    list_maladies,
    list_traitements,
    update_maladie,
    update_traitement,
)
from app.models.entities import User
from app.schemas.management import (
    CreateMaladieRequest,
    CreateTraitementRequest,
    UpdateMaladieRequest,
    UpdateTraitementRequest,
)
from app.schemas.sync import MaladieDTO, TraitementDTO

router = APIRouter(prefix="/knowledge")


# ============================================================================
# MALADIES (lecture publique, écriture admin)
# ============================================================================

@router.get("/maladies", response_model=list[MaladieDTO])
def get_maladies(db: Session = Depends(get_db)):
    """
    Liste toutes les maladies.

    Endpoint public pour consultation.
    """
    maladies = list_maladies(db)
    return [
        MaladieDTO(
            id=m.id,
            nomCommun=m.nom_commun,
            nomScientifique=m.nom_scientifique,
            description=m.description,
        )
        for m in maladies
    ]


@router.get("/maladies/{maladie_id}", response_model=MaladieDTO)
def get_maladie(maladie_id: int, db: Session = Depends(get_db)):
    """
    Récupère une maladie par son ID.
    """
    maladie = get_maladie_by_id(db, maladie_id)
    if not maladie:
        raise HTTPException(status_code=404, detail="Maladie introuvable")
    return MaladieDTO(
        id=maladie.id,
        nomCommun=maladie.nom_commun,
        nomScientifique=maladie.nom_scientifique,
        description=maladie.description,
    )


@router.post("/maladies", response_model=MaladieDTO)
def add_maladie(
    payload: CreateMaladieRequest,
    db: Session = Depends(get_db),
    _admin: User = Depends(get_current_admin),
):
    """
    Crée une nouvelle maladie (admin uniquement).
    """
    # Vérifier que l'ID n'existe pas déjà
    existing = get_maladie_by_id(db, payload.id)
    if existing:
        raise HTTPException(status_code=409, detail="Une maladie avec cet ID existe déjà")

    maladie = create_maladie(
        db,
        id=payload.id,
        nom_commun=payload.nomCommun,
        nom_scientifique=payload.nomScientifique,
        description=payload.description,
    )
    return MaladieDTO(
        id=maladie.id,
        nomCommun=maladie.nom_commun,
        nomScientifique=maladie.nom_scientifique,
        description=maladie.description,
    )


@router.put("/maladies/{maladie_id}", response_model=MaladieDTO)
def edit_maladie(
    maladie_id: int,
    payload: UpdateMaladieRequest,
    db: Session = Depends(get_db),
    _admin: User = Depends(get_current_admin),
):
    """
    Met à jour une maladie (admin uniquement).
    """
    maladie = update_maladie(
        db,
        maladie_id,
        nom_commun=payload.nomCommun,
        nom_scientifique=payload.nomScientifique,
        description=payload.description,
    )
    if not maladie:
        raise HTTPException(status_code=404, detail="Maladie introuvable")
    return MaladieDTO(
        id=maladie.id,
        nomCommun=maladie.nom_commun,
        nomScientifique=maladie.nom_scientifique,
        description=maladie.description,
    )


@router.delete("/maladies/{maladie_id}")
def remove_maladie(
    maladie_id: int,
    db: Session = Depends(get_db),
    _admin: User = Depends(get_current_admin),
):
    """
    Supprime une maladie (admin uniquement).

    Les traitements associés sont supprimés automatiquement (CASCADE).
    """
    deleted = delete_maladie(db, maladie_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Maladie introuvable")
    return {"message": "Maladie supprimée"}


# ============================================================================
# TRAITEMENTS
# ============================================================================

@router.get("/traitements", response_model=list[TraitementDTO])
def get_traitements(maladie_id: int | None = None, db: Session = Depends(get_db)):
    """
    Liste les traitements, optionnellement filtrés par maladie.
    """
    traitements = list_traitements(db, maladie_id)
    return [
        TraitementDTO(
            id=t.id,
            titre=t.titre,
            description=t.description,
            dosage=t.dosage,
            maladieId=t.maladie_id,
        )
        for t in traitements
    ]


@router.get("/traitements/{traitement_id}", response_model=TraitementDTO)
def get_traitement(traitement_id: int, db: Session = Depends(get_db)):
    """
    Récupère un traitement par son ID.
    """
    traitement = get_traitement_by_id(db, traitement_id)
    if not traitement:
        raise HTTPException(status_code=404, detail="Traitement introuvable")
    return TraitementDTO(
        id=traitement.id,
        titre=traitement.titre,
        description=traitement.description,
        dosage=traitement.dosage,
        maladieId=traitement.maladie_id,
    )


@router.post("/traitements", response_model=TraitementDTO)
def add_traitement(
    payload: CreateTraitementRequest,
    db: Session = Depends(get_db),
    _admin: User = Depends(get_current_admin),
):
    """
    Crée un nouveau traitement (admin uniquement).
    """
    # Vérifier que la maladie existe
    maladie = get_maladie_by_id(db, payload.maladieId)
    if not maladie:
        raise HTTPException(status_code=404, detail="Maladie introuvable")

    traitement = create_traitement(
        db,
        id=payload.id,
        titre=payload.titre,
        description=payload.description,
        dosage=payload.dosage,
        maladie_id=payload.maladieId,
    )
    return TraitementDTO(
        id=traitement.id,
        titre=traitement.titre,
        description=traitement.description,
        dosage=traitement.dosage,
        maladieId=traitement.maladie_id,
    )


@router.put("/traitements/{traitement_id}", response_model=TraitementDTO)
def edit_traitement(
    traitement_id: int,
    payload: UpdateTraitementRequest,
    db: Session = Depends(get_db),
    _admin: User = Depends(get_current_admin),
):
    """
    Met à jour un traitement (admin uniquement).
    """
    traitement = update_traitement(
        db,
        traitement_id,
        titre=payload.titre,
        description=payload.description,
        dosage=payload.dosage,
    )
    if not traitement:
        raise HTTPException(status_code=404, detail="Traitement introuvable")
    return TraitementDTO(
        id=traitement.id,
        titre=traitement.titre,
        description=traitement.description,
        dosage=traitement.dosage,
        maladieId=traitement.maladie_id,
    )


@router.delete("/traitements/{traitement_id}")
def remove_traitement(
    traitement_id: int,
    db: Session = Depends(get_db),
    _admin: User = Depends(get_current_admin),
):
    """
    Supprime un traitement (admin uniquement).
    """
    deleted = delete_traitement(db, traitement_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Traitement introuvable")
    return {"message": "Traitement supprimé"}
