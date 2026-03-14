"""
CRUD pour les diagnostics.

Gestion des diagnostics envoyés par les agriculteurs.
"""

import uuid
from datetime import datetime, timezone

from sqlalchemy.orm import Session

from app.models.entities import Diagnostic, Farmer, Location, PredictionResult
from app.schemas.sync import DiagnosticUploadDTO


def create_diagnostic_from_upload(db: Session, payload: DiagnosticUploadDTO) -> Diagnostic:
    """
    Crée un diagnostic à partir des données envoyées par le mobile.

    Cette fonction:
    1. Vérifie que l'agriculteur existe
    2. Crée la localisation si présente
    3. Crée le diagnostic
    4. Crée la prédiction si présente

    Args:
        db: Session de base de données
        payload: Données du diagnostic depuis le mobile

    Returns:
        Le diagnostic créé

    Raises:
        ValueError: Si l'agriculteur n'existe pas
    """
    # Vérifier que l'agriculteur existe
    farmer = db.query(Farmer).filter(Farmer.id == uuid.UUID(payload.userId)).first()
    if not farmer:
        raise ValueError("Agriculteur introuvable. Synchronisez d'abord votre profil.")

    # Créer la localisation si présente
    location_id = None
    if payload.location:
        location = Location(
            id=uuid.UUID(payload.location.id),
            latitude=payload.location.latitude,
            longitude=payload.location.longitude,
            region=payload.location.region,
            village=payload.location.village,
        )
        # merge() permet de créer ou mettre à jour si existe déjà
        db.merge(location)
        location_id = location.id

    # Convertir le timestamp milliseconds en datetime
    diagnostic_date = datetime.fromtimestamp(payload.date / 1000, tz=timezone.utc)

    # Convertir parcelleId si présent
    parcelle_id = None
    if payload.parcelleId:
        parcelle_id = uuid.UUID(payload.parcelleId)

    # Créer le diagnostic
    diagnostic = Diagnostic(
        id=uuid.UUID(payload.id),
        agriculteur_id=uuid.UUID(payload.userId),
        date=diagnostic_date,
        location_id=location_id,
        parcelle_id=parcelle_id,
    )
    db.merge(diagnostic)

    # Créer la prédiction si présente
    if payload.prediction:
        prediction = PredictionResult(
            id=uuid.UUID(payload.prediction.id),
            label=payload.prediction.label,
            confidence=payload.prediction.confidence,
            model_version=payload.prediction.modelVersion,
            maladie_id=payload.prediction.maladieId,
            diagnostic_id=diagnostic.id,
        )
        db.merge(prediction)
        # Lier la prédiction au diagnostic
        diagnostic.prediction_id = prediction.id

    db.commit()
    db.refresh(diagnostic)
    return diagnostic


def list_diagnostics(
    db: Session,
    agriculteur_id: str | None = None,
    limit: int = 100
) -> list[Diagnostic]:
    """
    Liste les diagnostics, optionnellement filtrés par agriculteur.

    Args:
        db: Session de base de données
        agriculteur_id: ID de l'agriculteur (optionnel)
        limit: Nombre maximum de résultats

    Returns:
        Liste des diagnostics triés par date décroissante
    """
    query = db.query(Diagnostic)

    if agriculteur_id:
        query = query.filter(Diagnostic.agriculteur_id == uuid.UUID(agriculteur_id))

    return query.order_by(Diagnostic.date.desc()).limit(limit).all()


def get_diagnostic_by_id(db: Session, diagnostic_id: str | uuid.UUID) -> Diagnostic | None:
    """
    Récupère un diagnostic par son ID.
    """
    if isinstance(diagnostic_id, str):
        diagnostic_id = uuid.UUID(diagnostic_id)
    return db.query(Diagnostic).filter(Diagnostic.id == diagnostic_id).first()


def validate_diagnostic(db: Session, diagnostic_id: str | uuid.UUID) -> Diagnostic | None:
    """
    Marque un diagnostic comme validé par un expert.
    """
    diagnostic = get_diagnostic_by_id(db, diagnostic_id)
    if diagnostic:
        diagnostic.expert_validated = True
        db.commit()
        db.refresh(diagnostic)
    return diagnostic


def delete_diagnostic(db: Session, diagnostic_id: str | uuid.UUID) -> bool:
    """
    Supprime un diagnostic.

    Returns:
        True si supprimé, False si non trouvé
    """
    diagnostic = get_diagnostic_by_id(db, diagnostic_id)
    if diagnostic:
        db.delete(diagnostic)
        db.commit()
        return True
    return False
