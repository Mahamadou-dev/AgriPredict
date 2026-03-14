from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import require_roles
from app.core.database import get_db
from app.crud.management import create_admin, delete_admin, list_admins, update_admin
from app.models.entities import UserRole
from app.schemas.management import (
    AdminCreateRequest,
    AdminPublic,
    AdminUpdateRequest,
    MessageResponse,
)

router = APIRouter(prefix="/admins")


@router.get("", response_model=list[AdminPublic])
def get_admins(db: Session = Depends(get_db), _=Depends(require_roles(UserRole.ADMIN))):
    rows = list_admins(db)
    return [
        AdminPublic(
            id=user.id,
            nom=user.nom,
            telephone=user.telephone,
            email=user.email,
            niveauAcces=admin.niveau_acces,
            isActive=user.is_active,
        )
        for user, admin in rows
    ]


@router.post("", response_model=AdminPublic)
def create_admin_route(
    payload: AdminCreateRequest,
    db: Session = Depends(get_db),
    _=Depends(require_roles(UserRole.ADMIN)),
):
    user, admin = create_admin(
        db,
        nom=payload.nom,
        telephone=payload.telephone,
        email=payload.email or "",
        password=payload.password,
        niveau_acces=payload.niveauAcces,
    )
    return AdminPublic(
        id=user.id,
        nom=user.nom,
        telephone=user.telephone,
        email=user.email,
        niveauAcces=admin.niveau_acces,
        isActive=user.is_active,
    )


@router.put("/{admin_id}", response_model=AdminPublic)
def update_admin_route(
    admin_id: str,
    payload: AdminUpdateRequest,
    db: Session = Depends(get_db),
    _=Depends(require_roles(UserRole.ADMIN)),
):
    row = update_admin(db, admin_id, payload.model_dump(exclude_unset=True))
    if not row:
        raise HTTPException(status_code=404, detail="Administrateur introuvable")
    user, admin = row
    return AdminPublic(
        id=user.id,
        nom=user.nom,
        telephone=user.telephone,
        email=user.email,
        niveauAcces=admin.niveau_acces,
        isActive=user.is_active,
    )


@router.delete("/{admin_id}", response_model=MessageResponse)
def delete_admin_route(
    admin_id: str,
    db: Session = Depends(get_db),
    _=Depends(require_roles(UserRole.ADMIN)),
):
    ok = delete_admin(db, admin_id)
    if not ok:
        raise HTTPException(status_code=404, detail="Administrateur introuvable")
    return MessageResponse(message="Administrateur supprime")
