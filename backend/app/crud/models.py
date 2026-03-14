"""
CRUD pour le modèle IA.

Gestion des versions du modèle TensorFlow Lite.
"""

from sqlalchemy.orm import Session

from app.models.entities import ModeleIA


def get_latest_model(db: Session) -> ModeleIA | None:
    """
    Récupère la dernière version du modèle IA.
    
    Triée par date de déploiement décroissante.
    """
    return db.query(ModeleIA).order_by(ModeleIA.date_deployment.desc()).first()


def get_model_by_version(db: Session, version: str) -> ModeleIA | None:
    """
    Récupère un modèle par sa version.
    """
    return db.query(ModeleIA).filter(ModeleIA.version == version).first()


def list_models(db: Session) -> list[ModeleIA]:
    """
    Liste tous les modèles disponibles.
    """
    return db.query(ModeleIA).order_by(ModeleIA.date_deployment.desc()).all()


def create_model(
    db: Session,
    version: str,
    framework: str = "tflite",
    precision: float = 0.0,
    input_size: int = 224,
) -> ModeleIA:
    """
    Enregistre une nouvelle version du modèle IA.
    """
    model = ModeleIA(
        version=version,
        framework=framework,
        precision=precision,
        input_size=input_size,
    )
    db.add(model)
    db.commit()
    db.refresh(model)
    return model


def delete_model(db: Session, version: str) -> bool:
    """
    Supprime une version du modèle.
    """
    model = get_model_by_version(db, version)
    if model:
        db.delete(model)
        db.commit()
        return True
    return False
