"""
Routes de synchronisation mobile ↔ serveur.

Ces endpoints sont les plus importants pour le mobile car ils permettent:
- L'envoi des données locales (UPLINK)
- La réception des mises à jour (DOWNLINK)

IMPORTANT: Les routes sont dupliquées avec des alias pour assurer
la compatibilité avec le mobile qui utilise les chemins /api/*.
"""

import uuid

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.crud.alerts import list_active_alerts
from app.crud.diagnostics import create_diagnostic_from_upload
from app.crud.knowledge import list_maladies, list_traitements
from app.crud.models import get_latest_model
from app.models.entities import Farmer, User, UserRole
from app.schemas.sync import (
    AlerteDTO,
    AppConfigDTO,
    CheckUpdatesResponseDTO,
    DiagnosticUploadDTO,
    DiagnosticUploadResponseDTO,
    KnowledgeBaseDTO,
    MaladieDTO,
    ModelUpdateDTO,
    TraitementDTO,
    UpdateBundleDTO,
    UserSyncDTO,
    dt_to_epoch_ms,
)

router = APIRouter()


# ============================================================================
# UPLINK - Envoi de données depuis le mobile
# ============================================================================

@router.post("/sync/profile", tags=["sync"])
@router.post("/api/users/sync", tags=["sync"])
def sync_profile(payload: UserSyncDTO, db: Session = Depends(get_db)):
    """
    Synchronise le profil agriculteur + ses parcelles vers le serveur.

    Cette route reçoit:
    - Identité (nomPrenom, téléphone)
    - Parcelles (nomParcelle, commune, village, ville)

    Si l'utilisateur existe déjà (même ID), ses données sont mises à jour.
    Sinon, un nouveau compte est créé (sync uniquement).
    Les parcelles sont créées ou mises à jour (merge).
    """
    user_uuid = uuid.UUID(payload.id)
    
    # Chercher un utilisateur existant
    user = db.query(User).filter(User.id == user_uuid).first()
    
    if user:
        # Mettre à jour les données existantes
        user.nom = payload.nomPrenom
        user.telephone = payload.telephone
    else:
        # Créer un nouvel utilisateur (sync uniquement)
        user = User(
            id=user_uuid,
            nom=payload.nomPrenom,
            telephone=payload.telephone,
            email="",
            password_hash="SYNC_ONLY",  # Ce compte ne peut pas se connecter
            role=UserRole.AGRICULTEUR,
            is_active=True,
        )
        db.add(user)
    
    db.flush()
    
    # Mettre à jour ou créer le profil agriculteur
    farmer = db.query(Farmer).filter(Farmer.id == user.id).first()
    if not farmer:
        farmer = Farmer(id=user.id)
        db.add(farmer)
        db.flush()

    # Synchroniser les parcelles
    for p in payload.parcelles:
        parcelle_uuid = uuid.UUID(p.id)
        existing_parcelle = db.query(Parcelle).filter(Parcelle.id == parcelle_uuid).first()
        if existing_parcelle:
            existing_parcelle.nom_parcelle = p.nomParcelle
            existing_parcelle.commune = p.commune
            existing_parcelle.village = p.village
            existing_parcelle.ville = p.ville
        else:
            new_parcelle = Parcelle(
                id=parcelle_uuid,
                nom_parcelle=p.nomParcelle,
                commune=p.commune,
                village=p.village,
                ville=p.ville,
                agriculteur_id=user.id,
            )
            db.add(new_parcelle)

    db.commit()

    return {"success": True, "userId": str(user.id)}


@router.post("/sync/diagnostics", response_model=DiagnosticUploadResponseDTO, tags=["sync"])
@router.post("/api/diagnostics/upload", response_model=DiagnosticUploadResponseDTO, tags=["sync"])
def sync_diagnostics(payload: DiagnosticUploadDTO, db: Session = Depends(get_db)):
    """
    Reçoit un diagnostic envoyé par le mobile.
    
    Le diagnostic contient:
    - Métadonnées (ID, date, utilisateur)
    - Localisation GPS (optionnel)
    - Résultat de la prédiction IA (label, confiance)
    
    L'agriculteur doit d'abord avoir synchronisé son profil.
    """
    diagnostic = create_diagnostic_from_upload(db, payload)
    return DiagnosticUploadResponseDTO(
        success=True, 
        diagnosticId=str(diagnostic.id), 
        message="Diagnostic reçu avec succès"
    )


# ============================================================================
# DOWNLINK - Envoi de données vers le mobile
# ============================================================================

@router.get("/sync/check-updates", response_model=CheckUpdatesResponseDTO, tags=["sync"])
@router.get("/api/updates/check", response_model=CheckUpdatesResponseDTO, tags=["sync"])
def check_updates(
    knowledgeVersion: str = "",
    modelVersion: str = "",
    _db: Session = Depends(get_db),
):
    """
    Vérifie si des mises à jour sont disponibles.
    
    Le mobile envoie ses versions locales:
    - knowledgeVersion: version de la base de connaissances
    - modelVersion: version du modèle IA
    
    Le serveur répond avec les versions actuelles.
    Si différentes, le mobile sait qu'il doit télécharger les mises à jour.
    """
    # Version actuelle sur le serveur
    current_kb_version = "1.0.0"
    current_model_version = "1.0.0"
    current_config_version = "1.0.0"
    
    # Déterminer si une mise à jour est nécessaire
    has_update = (
        (knowledgeVersion or "") != current_kb_version or
        (modelVersion or "") != current_model_version
    )
    
    return CheckUpdatesResponseDTO(
        hasUpdate=has_update,
        knowledgeBaseVersion=current_kb_version,
        modelVersion=current_model_version,
        appConfigVersion=current_config_version,
    )


@router.get("/sync/knowledge-base", response_model=KnowledgeBaseDTO, tags=["sync"])
def sync_knowledge_base(db: Session = Depends(get_db)):
    """
    Télécharge la base de connaissances complète.
    
    Contient toutes les maladies et leurs traitements recommandés.
    Le mobile stocke ces données localement pour consultation hors-ligne.
    """
    # Récupérer toutes les maladies
    maladies = [
        MaladieDTO(
            id=m.id,
            nomCommun=m.nom_commun,
            nomScientifique=m.nom_scientifique,
            description=m.description,
        )
        for m in list_maladies(db)
    ]
    
    # Récupérer tous les traitements
    traitements = [
        TraitementDTO(
            id=t.id,
            titre=t.titre,
            description=t.description,
            dosage=t.dosage,
            maladieId=t.maladie_id,
        )
        for t in list_traitements(db)
    ]
    
    return KnowledgeBaseDTO(
        version="1.0.0",
        maladies=maladies,
        traitements=traitements,
    )


@router.get("/sync/alerts", response_model=list[AlerteDTO], tags=["sync"])
def sync_alerts(db: Session = Depends(get_db)):
    """
    Télécharge les alertes agricoles actives.
    
    Seules les alertes non expirées sont retournées.
    """
    return [
        AlerteDTO(
            id=str(a.id),
            message=a.message,
            zone=a.zone,
            gravite=a.gravite,
            dateEmission=dt_to_epoch_ms(a.date_emission) or 0,
            dateExpiration=dt_to_epoch_ms(a.date_expiration),
            maladieId=a.maladie_id,
        )
        for a in list_active_alerts(db)
    ]


@router.get("/sync/model-updates", response_model=ModelUpdateDTO | None, tags=["sync"])
def sync_model_updates(db: Session = Depends(get_db)):
    """
    Récupère les informations sur la dernière version du modèle IA.
    
    Si aucun modèle n'est enregistré, retourne None.
    """
    model = get_latest_model(db)
    if not model:
        return None
    
    return ModelUpdateDTO(
        version=model.version,
        downloadUrl=f"/models/{model.version}/download",
        framework=model.framework,
        precision=model.precision,
        inputSize=model.input_size,
        checksum="",  # TODO: implémenter le checksum
    )


@router.get("/sync/app-config", response_model=AppConfigDTO, tags=["sync"])
def sync_app_config():
    """
    Récupère la configuration de l'application.
    
    Permet de modifier le comportement du mobile sans mise à jour.
    """
    return AppConfigDTO(
        version="1.0.0",
        syncIntervalMinutes=60,
        maxImageSizeMb=5,
        features={
            "expert_contact": True,
            "offline_mode": True,
            "alerts": True,
        },
    )


@router.get("/sync/download", response_model=UpdateBundleDTO, tags=["sync"])
@router.get("/api/updates/download", response_model=UpdateBundleDTO, tags=["sync"])
def download_updates(db: Session = Depends(get_db)):
    """
    Télécharge toutes les mises à jour en un seul appel.
    
    C'est le moyen le plus efficace pour le mobile de tout récupérer
    en une seule requête réseau.
    """
    # Récupérer chaque composant
    kb = sync_knowledge_base(db)
    alerts = sync_alerts(db)
    model = sync_model_updates(db)
    config = sync_app_config()
    
    return UpdateBundleDTO(
        knowledgeBase=kb,
        alertes=alerts,
        modelUpdate=model,
        appConfig=config,
    )
