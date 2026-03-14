"""
Schémas Pydantic communs (réponses publiques).

Ces schémas sont utilisés pour formater les réponses API
de manière cohérente.
"""

from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field


class UserPublic(BaseModel):
    """
    Informations publiques d'un utilisateur.
    
    Utilisé pour afficher les données du profil sans exposer
    d'informations sensibles (mot de passe, etc.).
    """
    model_config = ConfigDict(from_attributes=True)
    
    id: UUID = Field(..., description="Identifiant unique")
    nom: str = Field(..., description="Nom complet")
    telephone: str = Field(..., description="Numéro de téléphone")
    email: str = Field(default="", description="Email")
    role: str = Field(..., description="Rôle (AGRICULTEUR, EXPERT, ADMIN)")
    isActive: bool = Field(default=True, alias="is_active", description="Compte actif")
    createdAt: datetime = Field(..., alias="created_at", description="Date de création")


class FarmerPublic(UserPublic):
    """
    Informations publiques d'un agriculteur.
    
    Étend UserPublic avec les champs spécifiques à l'agriculteur.
    """
    commune: str = Field(default="", description="Commune de résidence")
    village: str = Field(default="", description="Village de résidence")


class ExpertPublic(UserPublic):
    """
    Informations publiques d'un expert.
    """
    specialite: str = Field(default="", description="Domaine d'expertise")
    matricule: str = Field(..., description="Numéro de matricule")
    commune: str = Field(default="", description="Zone d'intervention")


class DiagnosticPublic(BaseModel):
    """
    Informations publiques d'un diagnostic.
    """
    model_config = ConfigDict(from_attributes=True)
    
    id: UUID
    agriculteurId: UUID = Field(..., alias="agriculteur_id")
    date: datetime
    expertValidated: bool = Field(default=False, alias="expert_validated")
    createdAt: datetime = Field(..., alias="created_at")


class AlertPublic(BaseModel):
    """
    Informations publiques d'une alerte.
    """
    model_config = ConfigDict(from_attributes=True)
    
    id: UUID
    message: str
    zone: str = ""
    gravite: float = 0.0
    dateEmission: datetime = Field(..., alias="date_emission")
    dateExpiration: datetime | None = Field(default=None, alias="date_expiration")
    maladieId: int | None = Field(default=None, alias="maladie_id")
