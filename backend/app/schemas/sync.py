"""
Schémas Pydantic pour la synchronisation mobile ↔ serveur.

Ces DTOs (Data Transfer Objects) correspondent EXACTEMENT aux DTOs
définis côté mobile Kotlin pour assurer la compatibilité.

UPLINK  = Mobile → Serveur (envoi de données)
DOWNLINK = Serveur → Mobile (réception de données)
"""

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


# ============================================================================
# UTILITAIRES
# ============================================================================

def dt_to_epoch_ms(value: datetime | None) -> int | None:
    """
    Convertit une datetime en timestamp milliseconds (format mobile).
    
    Le mobile Android utilise des timestamps en millisecondes,
    alors que Python utilise des secondes par défaut.
    """
    if value is None:
        return None
    return int(value.timestamp() * 1000)


# ============================================================================
# UPLINK - Données envoyées par le mobile
# ============================================================================

class ParcelleSyncDTO(BaseModel):
    """DTO pour synchroniser une parcelle depuis le mobile."""
    id: str = Field(..., description="UUID de la parcelle")
    nomParcelle: str = Field(..., description="Nom de la parcelle")
    commune: str = Field(default="", description="Commune de la parcelle")
    village: str = Field(default="", description="Village de la parcelle")
    ville: str = Field(default="", description="Ville la plus proche")


class UserSyncDTO(BaseModel):
    """
    DTO pour synchroniser le profil agriculteur vers le serveur.
    
    Contient les données minimales nécessaires:
    - Identification (id, téléphone)
    - Contact (nomPrenom)
    - Parcelles (commune, village, ville par parcelle)
    """
    id: str = Field(..., description="UUID de l'utilisateur mobile")
    nomPrenom: str = Field(..., description="Nom et prénom")
    telephone: str = Field(..., description="Numéro de téléphone")
    parcelles: list[ParcelleSyncDTO] = Field(default_factory=list, description="Parcelles de l'agriculteur")


class LocationUploadDTO(BaseModel):
    """Coordonnées GPS d'un diagnostic."""
    id: str
    latitude: float
    longitude: float
    region: str = ""
    village: str = ""


class PredictionUploadDTO(BaseModel):
    """Résultat de la prédiction IA locale."""
    id: str
    label: str = Field(..., description="Label prédit (ex: 'Tomato___Early_blight')")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Score de confiance")
    modelVersion: str = Field(default="", description="Version du modèle TFLite")
    maladieId: int | None = Field(default=None, description="ID de la maladie mappée")


class DiagnosticUploadDTO(BaseModel):
    """
    DTO complet pour envoyer un diagnostic au serveur.
    
    Regroupe le diagnostic, la localisation et la prédiction
    en un seul objet pour simplifier l'envoi.
    """
    id: str = Field(..., description="UUID du diagnostic")
    userId: str = Field(..., description="UUID de l'agriculteur")
    date: int = Field(..., description="Timestamp en millisecondes")
    parcelleId: str | None = Field(default=None, description="UUID de la parcelle")
    location: LocationUploadDTO | None = Field(default=None, description="GPS (optionnel)")
    prediction: PredictionUploadDTO | None = Field(default=None, description="Résultat IA")


class DiagnosticUploadResponseDTO(BaseModel):
    """Réponse après réception d'un diagnostic."""
    success: bool
    diagnosticId: str
    message: str = ""


# ============================================================================
# DOWNLINK - Données envoyées vers le mobile
# ============================================================================

class CheckUpdatesResponseDTO(BaseModel):
    """
    Réponse à la vérification des mises à jour.
    
    Le mobile compare ses versions locales avec celles du serveur
    pour savoir s'il doit télécharger des mises à jour.
    """
    hasUpdate: bool = Field(..., description="Y a-t-il des mises à jour disponibles ?")
    knowledgeBaseVersion: str = Field(default="", description="Version de la base de connaissances")
    modelVersion: str = Field(default="", description="Version du modèle IA")
    appConfigVersion: str = Field(default="", description="Version de la configuration")


class MaladieDTO(BaseModel):
    """DTO d'une maladie pour le mobile."""
    model_config = ConfigDict(from_attributes=True)

    id: int
    nomCommun: str = Field(..., description="Nom courant de la maladie")
    nomScientifique: str = Field(default="", description="Nom scientifique latin")
    description: str = Field(default="", description="Description détaillée")


class TraitementDTO(BaseModel):
    """DTO d'un traitement pour le mobile."""
    model_config = ConfigDict(from_attributes=True)

    id: int
    titre: str = Field(..., description="Titre du traitement")
    description: str = Field(default="", description="Description détaillée")
    dosage: str = Field(default="", description="Dosage recommandé")
    maladieId: int = Field(..., description="ID de la maladie associée")


class KnowledgeBaseDTO(BaseModel):
    """
    Bundle complet de la base de connaissances.
    
    Contient toutes les maladies et traitements pour la
    consultation hors-ligne sur le mobile.
    """
    version: str = Field(..., description="Version de la base")
    maladies: list[MaladieDTO] = Field(default_factory=list)
    traitements: list[TraitementDTO] = Field(default_factory=list)


class AlerteDTO(BaseModel):
    """DTO d'une alerte pour le mobile."""
    id: str
    message: str
    zone: str = ""
    gravite: float = Field(default=0.0, ge=0.0, le=1.0)
    dateEmission: int = Field(..., description="Timestamp en millisecondes")
    dateExpiration: int | None = None
    maladieId: int | None = None


class ModelUpdateDTO(BaseModel):
    """
    Informations de mise à jour du modèle IA.
    
    Le mobile peut télécharger une nouvelle version du modèle
    TFLite si disponible.
    """
    version: str
    downloadUrl: str = Field(..., description="URL de téléchargement du modèle")
    framework: str = "tflite"
    precision: float = Field(default=0.0, description="Précision du modèle (%)")
    inputSize: int = Field(default=224, description="Taille d'entrée en pixels")
    checksum: str = Field(default="", description="Hash MD5 pour vérification")


class AppConfigDTO(BaseModel):
    """
    Configuration de l'application mobile.
    
    Permet de modifier le comportement de l'app sans mise à jour.
    """
    version: str
    syncIntervalMinutes: int = Field(default=60, description="Intervalle de sync en minutes")
    maxImageSizeMb: int = Field(default=5, description="Taille max d'image en Mo")
    features: dict[str, bool] = Field(default_factory=dict, description="Feature flags")


class UpdateBundleDTO(BaseModel):
    """
    Bundle regroupant toutes les mises à jour disponibles.
    
    Permet au mobile de tout télécharger en une seule requête.
    """
    knowledgeBase: KnowledgeBaseDTO | None = None
    alertes: list[AlerteDTO] = Field(default_factory=list)
    modelUpdate: ModelUpdateDTO | None = None
    appConfig: AppConfigDTO | None = None

