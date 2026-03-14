"""
Routes pour les alertes agricoles.

Ces endpoints permettent de gérer les alertes régionales
créées par les experts et administrateurs.
"""

from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_expert, get_current_user
from app.core.database import get_db
from app.crud.alerts import (
    create_alert,
    delete_alert,
    get_alert_by_id,
    list_active_alerts,
    list_alerts,
    update_alert,
)
from app.models.entities import User
from app.schemas.common import AlertPublic
from app.schemas.management import CreateAlertRequest, UpdateAlertRequest

router = APIRouter(prefix="/alerts")


@router.get("/", response_model=list[AlertPublic])
def get_alerts(
    zone: str | None = None,
    active_only: bool = True,
    db: Session = Depends(get_db),
):
    """
    Liste les alertes.

    Par défaut, seules les alertes actives (non expirées) sont retournées.

    Paramètres:
    - zone: Filtre par zone géographique (recherche partielle)
    - active_only: Si True, exclut les alertes expirées
    """
    if active_only:
        alerts = list_active_alerts(db, zone)
    else:
        alerts = list_alerts(db, zone)

    return [
        AlertPublic(
            id=a.id,
            message=a.message,
            zone=a.zone,
            gravite=a.gravite,
            date_emission=a.date_emission,
            date_expiration=a.date_expiration,
            maladie_id=a.maladie_id,
        )
        for a in alerts
    ]


@router.get("/{alert_id}", response_model=AlertPublic)
def get_alert(alert_id: str, db: Session = Depends(get_db)):
    """
    Récupère une alerte par son ID.
    """
    alert = get_alert_by_id(db, alert_id)
    if not alert:
        raise HTTPException(status_code=404, detail="Alerte introuvable")
    return AlertPublic(
        id=alert.id,
        message=alert.message,
        zone=alert.zone,
        gravite=alert.gravite,
        date_emission=alert.date_emission,
        date_expiration=alert.date_expiration,
        maladie_id=alert.maladie_id,
    )


@router.post("/", response_model=AlertPublic)
def add_alert(
    payload: CreateAlertRequest,
    db: Session = Depends(get_db),
    user: User = Depends(get_current_expert),
):
    """
    Crée une nouvelle alerte (expert ou admin uniquement).

    L'expert créateur est automatiquement enregistré.
    """
    # Convertir le timestamp d'expiration si fourni
    expiration = None
    if payload.dateExpiration:
        expiration = datetime.fromtimestamp(
            payload.dateExpiration / 1000, tz=timezone.utc
        )

    alert = create_alert(
        db,
        message=payload.message,
        zone=payload.zone,
        gravite=payload.gravite,
        expert_id=user.id if user.expert else None,
        maladie_id=payload.maladieId,
        date_expiration=expiration,
    )
    return AlertPublic(
        id=alert.id,
        message=alert.message,
        zone=alert.zone,
        gravite=alert.gravite,
        date_emission=alert.date_emission,
        date_expiration=alert.date_expiration,
        maladie_id=alert.maladie_id,
    )


@router.put("/{alert_id}", response_model=AlertPublic)
def edit_alert(
    alert_id: str,
    payload: UpdateAlertRequest,
    db: Session = Depends(get_db),
    _user: User = Depends(get_current_expert),
):
    """
    Met à jour une alerte (expert ou admin uniquement).
    """
    # Convertir le timestamp d'expiration si fourni
    expiration = None
    if payload.dateExpiration:
        expiration = datetime.fromtimestamp(
            payload.dateExpiration / 1000, tz=timezone.utc
        )

    alert = update_alert(
        db,
        alert_id,
        message=payload.message,
        zone=payload.zone,
        gravite=payload.gravite,
        maladie_id=payload.maladieId,
        date_expiration=expiration,
    )
    if not alert:
        raise HTTPException(status_code=404, detail="Alerte introuvable")

    return AlertPublic(
        id=alert.id,
        message=alert.message,
        zone=alert.zone,
        gravite=alert.gravite,
        date_emission=alert.date_emission,
        date_expiration=alert.date_expiration,
        maladie_id=alert.maladie_id,
    )


@router.delete("/{alert_id}")
def remove_alert(
    alert_id: str,
    db: Session = Depends(get_db),
    _user: User = Depends(get_current_expert),
):
    """
    Supprime une alerte (expert ou admin uniquement).
    """
    deleted = delete_alert(db, alert_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Alerte introuvable")
    return {"message": "Alerte supprimée"}
