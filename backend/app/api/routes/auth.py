from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.deps import get_current_user
from app.core.database import get_db
from app.core.security import create_access_token
from app.crud.auth import authenticate, create_farmer_user, get_user_by_phone
from app.crud.management import change_password, update_farmer_profile
from app.schemas.auth import LoginRequest, RegisterRequest, TokenResponse
from app.schemas.common import UserPublic
from app.schemas.management import ChangePasswordRequest, MessageResponse, ProfileUpdateRequest

router = APIRouter(prefix="/auth")


@router.post("/register", response_model=UserPublic)
def register(payload: RegisterRequest, db: Session = Depends(get_db)):
    existing = get_user_by_phone(db, payload.telephone)
    if existing:
        raise HTTPException(status_code=409, detail="Telephone deja utilise")
    user = create_farmer_user(
        db,
        nom=payload.nomPrenom,
        telephone=payload.telephone,
        password=payload.password,
    )
    return {
        "id": user.id,
        "nom": user.nom,
        "telephone": user.telephone,
        "email": user.email,
        "role": user.role.value,
        "isActive": user.is_active,
        "createdAt": user.created_at,
    }


@router.post("/login", response_model=TokenResponse)
def login(payload: LoginRequest, db: Session = Depends(get_db)):
    user = authenticate(db, payload.telephone, payload.password)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Identifiants invalides")
    token = create_access_token(subject=user.telephone)
    return TokenResponse(access_token=token)


@router.get("/me", response_model=UserPublic)
def me(user=Depends(get_current_user)):
    return {
        "id": user.id,
        "nom": user.nom,
        "telephone": user.telephone,
        "email": user.email,
        "role": user.role.value,
        "isActive": user.is_active,
        "createdAt": user.created_at,
    }


@router.put("/me", response_model=UserPublic)
def update_me(payload: ProfileUpdateRequest, db: Session = Depends(get_db), user=Depends(get_current_user)):
    updated = update_farmer_profile(
        db,
        user=user,
        nom=payload.nom,
        telephone=payload.telephone,
    )
    return {
        "id": updated.id,
        "nom": updated.nom,
        "telephone": updated.telephone,
        "email": updated.email,
        "role": updated.role.value,
        "isActive": updated.is_active,
        "createdAt": updated.created_at,
    }


@router.post("/change-password", response_model=MessageResponse)
def change_my_password(
    payload: ChangePasswordRequest,
    db: Session = Depends(get_db),
    user=Depends(get_current_user),
):
    ok = change_password(
        db,
        user=user,
        old_password=payload.oldPassword,
        new_password=payload.newPassword,
    )
    if not ok:
        raise HTTPException(status_code=400, detail="Ancien mot de passe incorrect")
    return MessageResponse(message="Mot de passe modifie")
