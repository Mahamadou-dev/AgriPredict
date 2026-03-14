"""
CRUD pour les alertes agricoles.

Gestion des alertes créées par les experts et administrateurs.
"""

import uuid
from datetime import datetime, timezone

from sqlalchemy.orm import Session

from app.models.entities import Alert


def list_alerts(
    db: Session,
    zone: str | None = None,
    limit: int = 50
) -> list[Alert]:
    """
    Liste les alertes, optionnellement filtrées par zone.
    """
    query = db.query(Alert)
    
    if zone:
        query = query.filter(Alert.zone.ilike(f"%{zone}%"))
    
    return query.order_by(Alert.date_emission.desc()).limit(limit).all()


def list_active_alerts(db: Session, zone: str | None = None) -> list[Alert]:
    """
    Liste les alertes actives (non expirées).
    
    Une alerte est active si:
    - Elle n'a pas de date d'expiration, ou
    - Sa date d'expiration est dans le futur
    """
    now = datetime.now(timezone.utc)
    query = db.query(Alert).filter(
        (Alert.date_expiration == None) | (Alert.date_expiration > now)
    )
    
    if zone:
        query = query.filter(Alert.zone.ilike(f"%{zone}%"))
    
    return query.order_by(Alert.gravite.desc(), Alert.date_emission.desc()).all()


def get_alert_by_id(db: Session, alert_id: str | uuid.UUID) -> Alert | None:
    """
    Récupère une alerte par son ID.
    """
    if isinstance(alert_id, str):
        alert_id = uuid.UUID(alert_id)
    return db.query(Alert).filter(Alert.id == alert_id).first()


def create_alert(
    db: Session,
    message: str,
    zone: str = "",
    gravite: float = 0.5,
    expert_id: uuid.UUID | None = None,
    maladie_id: int | None = None,
    date_expiration: datetime | None = None,
) -> Alert:
    """
    Crée une nouvelle alerte.
    
    Args:
        db: Session de base de données
        message: Contenu de l'alerte
        zone: Zone géographique concernée
        gravite: Niveau de gravité (0.0 à 1.0)
        expert_id: ID de l'expert créateur (optionnel)
        maladie_id: ID de la maladie liée (optionnel)
        date_expiration: Date d'expiration (optionnel)
    """
    alert = Alert(
        message=message,
        zone=zone,
        gravite=gravite,
        expert_id=expert_id,
        maladie_id=maladie_id,
        date_expiration=date_expiration,
    )
    db.add(alert)
    db.commit()
    db.refresh(alert)
    return alert


def update_alert(
    db: Session,
    alert_id: str | uuid.UUID,
    message: str | None = None,
    zone: str | None = None,
    gravite: float | None = None,
    maladie_id: int | None = None,
    date_expiration: datetime | None = None,
) -> Alert | None:
    """
    Met à jour une alerte existante.
    """
    alert = get_alert_by_id(db, alert_id)
    if not alert:
        return None
    
    if message is not None:
        alert.message = message
    if zone is not None:
        alert.zone = zone
    if gravite is not None:
        alert.gravite = gravite
    if maladie_id is not None:
        alert.maladie_id = maladie_id
    if date_expiration is not None:
        alert.date_expiration = date_expiration
    
    db.commit()
    db.refresh(alert)
    return alert


def delete_alert(db: Session, alert_id: str | uuid.UUID) -> bool:
    """
    Supprime une alerte.
    """
    alert = get_alert_by_id(db, alert_id)
    if alert:
        db.delete(alert)
        db.commit()
        return True
    return False
