"""
Schémas Pydantic pour la gestion (admin/expert/profil).

Ces schémas sont utilisés par les routes de gestion des utilisateurs.
"""

from pydantic import BaseModel, Field


class MessageResponse(BaseModel):
    """Réponse simple avec un message."""
    message: str


class ProfileUpdateRequest(BaseModel):
    """Mise à jour du profil utilisateur (nom et téléphone uniquement)."""
    nom: str = Field(..., min_length=2, max_length=120)
    telephone: str = Field(..., min_length=8, max_length=30)


class ChangePasswordRequest(BaseModel):
    """Changement de mot de passe."""
    oldPassword: str = Field(..., description="Ancien mot de passe")
    newPassword: str = Field(..., min_length=4, description="Nouveau mot de passe")


class CreateExpertRequest(BaseModel):
    """Création d'un expert par un admin."""
    nom: str = Field(..., min_length=2, max_length=120)
    telephone: str = Field(..., min_length=8, max_length=30)
    email: str = ""
    password: str = Field(..., min_length=4)
    specialite: str = Field(default="", description="Domaine d'expertise")
    matricule: str = Field(..., description="Numéro de matricule unique")
    commune: str = Field(default="", description="Zone d'intervention")


class UpdateExpertRequest(BaseModel):
    """Mise à jour d'un expert."""
    nom: str | None = None
    telephone: str | None = None
    email: str | None = None
    specialite: str | None = None
    commune: str | None = None


class CreateAdminRequest(BaseModel):
    """Création d'un administrateur."""
    nom: str = Field(..., min_length=2, max_length=120)
    telephone: str = Field(..., min_length=8, max_length=30)
    email: str = ""
    password: str = Field(..., min_length=4)
    niveauAcces: int = Field(default=1, ge=1, description="Niveau d'accès (1 = standard)")


class UpdateAdminRequest(BaseModel):
    """Mise à jour d'un administrateur."""
    nom: str | None = None
    telephone: str | None = None
    email: str | None = None
    niveauAcces: int | None = None


class CreateAlertRequest(BaseModel):
    """Création d'une alerte agricole."""
    message: str = Field(..., min_length=10, description="Contenu de l'alerte")
    zone: str = Field(default="", description="Zone géographique concernée")
    gravite: float = Field(default=0.5, ge=0.0, le=1.0, description="Niveau de gravité")
    dateExpiration: int | None = Field(default=None, description="Timestamp d'expiration")
    maladieId: int | None = Field(default=None, description="Maladie liée (optionnel)")


class UpdateAlertRequest(BaseModel):
    """Mise à jour d'une alerte."""
    message: str | None = None
    zone: str | None = None
    gravite: float | None = Field(default=None, ge=0.0, le=1.0)
    dateExpiration: int | None = None
    maladieId: int | None = None


class CreateMaladieRequest(BaseModel):
    """Création d'une maladie."""
    id: int = Field(..., description="ID fixe (doit correspondre au modèle IA)")
    nomCommun: str = Field(..., min_length=2)
    nomScientifique: str = ""
    description: str = ""


class UpdateMaladieRequest(BaseModel):
    """Mise à jour d'une maladie."""
    nomCommun: str | None = None
    nomScientifique: str | None = None
    description: str | None = None


class CreateTraitementRequest(BaseModel):
    """Création d'un traitement."""
    id: int = Field(..., description="ID fixe")
    titre: str = Field(..., min_length=2)
    description: str = ""
    dosage: str = ""
    maladieId: int = Field(..., description="ID de la maladie associée")


class UpdateTraitementRequest(BaseModel):
    """Mise à jour d'un traitement."""
    titre: str | None = None
    description: str | None = None
    dosage: str | None = None
