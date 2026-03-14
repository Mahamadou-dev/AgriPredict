from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import require_roles
from app.core.database import get_db
from app.crud.experts import list_experts
from app.crud.management import create_expert, delete_expert, update_expert
from app.models.entities import UserRole
from app.schemas.common import ExpertPublic
from app.schemas.management import ExpertCreateRequest, ExpertUpdateRequest, MessageResponse

router = APIRouter(prefix="/experts")


@router.get("", response_model=list[ExpertPublic])
def get_experts(db: Session = Depends(get_db)):
    rows = list_experts(db)
    return [
        ExpertPublic(
            id=user.id,
            nom=user.nom,
            telephone=user.telephone,
            specialite=expert.specialite,
            commune=expert.commune,
        )
        for user, expert in rows
    ]


@router.post("", response_model=ExpertPublic)
def create_expert_route(
    payload: ExpertCreateRequest,
    db: Session = Depends(get_db),
    _=Depends(require_roles(UserRole.ADMIN)),
):
    user, expert = create_expert(
        db,
        nom=payload.nom,
        telephone=payload.telephone,
        email=payload.email or "",
        password=payload.password,
        specialite=payload.specialite,
        matricule=payload.matricule,
        commune=payload.commune,
    )
    return ExpertPublic(
        id=user.id,
        nom=user.nom,
        telephone=user.telephone,
        specialite=expert.specialite,
        commune=expert.commune,
    )


@router.put("/{expert_id}", response_model=ExpertPublic)
def update_expert_route(
    expert_id: str,
    payload: ExpertUpdateRequest,
    db: Session = Depends(get_db),
    _=Depends(require_roles(UserRole.ADMIN)),
):
    row = update_expert(db, expert_id, payload.model_dump(exclude_unset=True))
    if not row:
        raise HTTPException(status_code=404, detail="Expert introuvable")
    user, expert = row
    return ExpertPublic(
        id=user.id,
        nom=user.nom,
        telephone=user.telephone,
        specialite=expert.specialite,
        commune=expert.commune,
    )


@router.delete("/{expert_id}", response_model=MessageResponse)
def delete_expert_route(
    expert_id: str,
    db: Session = Depends(get_db),
    _=Depends(require_roles(UserRole.ADMIN)),
):
    ok = delete_expert(db, expert_id)
    if not ok:
        raise HTTPException(status_code=404, detail="Expert introuvable")
    return MessageResponse(message="Expert supprime")
