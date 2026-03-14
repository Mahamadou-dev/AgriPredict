"""
Modèles SQLAlchemy - Entités de la base de données AgriPredict.

Ces classes représentent les tables PostgreSQL.
SQLAlchemy 2.0 utilise les annotations de type Python pour définir les colonnes.

Structure:
- User (Utilisateur) : compte utilisateur générique
- Farmer (Agriculteur) : extension de User pour les agriculteurs
- Expert : extension de User pour les experts agricoles
- Admin (Administrateur) : extension de User pour les admins
- Diagnostic : analyse effectuée par un agriculteur
- Image : photo associée à un diagnostic
- Location : coordonnées GPS d'un diagnostic
- PredictionResult : résultat de l'analyse IA
- Maladie : référentiel des maladies de plantes
- Traitement : traitements recommandés par maladie
- Alert (Alerte) : alertes agricoles régionales
- ModeleIA : informations sur le modèle TensorFlow Lite
"""

import enum
import uuid
from datetime import datetime, timezone

from sqlalchemy import (
    Boolean,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base


def utc_now() -> datetime:
    """Retourne la date/heure actuelle en UTC."""
    return datetime.now(timezone.utc)


# ============================================================================
# ÉNUMÉRATIONS
# ============================================================================

class UserRole(str, enum.Enum):
    """
    Rôles des utilisateurs dans le système.
    
    - AGRICULTEUR : utilisateur mobile qui effectue des diagnostics
    - EXPERT : expert agricole qui peut valider les diagnostics et créer des alertes
    - ADMIN : administrateur avec accès complet au système
    """
    AGRICULTEUR = "AGRICULTEUR"
    EXPERT = "EXPERT"
    ADMIN = "ADMIN"


# ============================================================================
# UTILISATEURS ET PROFILS
# ============================================================================

class User(Base):
    """
    Table principale des utilisateurs.
    
    Contient les informations communes à tous les types d'utilisateurs.
    Les données spécifiques sont dans les tables Farmer, Expert ou Admin.
    
    C'est le pattern "Table par sous-classe" (joined table inheritance).
    """
    __tablename__ = "users"

    # Identifiant unique (UUID v4 généré automatiquement)
    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        primary_key=True, 
        default=uuid.uuid4
    )
    
    # Nom complet de l'utilisateur
    nom: Mapped[str] = mapped_column(String(120), nullable=False)
    
    # Numéro de téléphone (unique, utilisé pour la connexion)
    telephone: Mapped[str] = mapped_column(
        String(30), 
        unique=True, 
        index=True, 
        nullable=False
    )
    
    # Email (optionnel)
    email: Mapped[str] = mapped_column(String(255), default="", nullable=False)
    
    # Hash du mot de passe (jamais stocké en clair!)
    password_hash: Mapped[str] = mapped_column(
        "passwordHash", 
        String(255), 
        nullable=False
    )
    
    # Rôle de l'utilisateur
    role: Mapped[UserRole] = mapped_column(
        Enum(UserRole, name="user_role"), 
        nullable=False
    )
    
    # Compte actif ou désactivé
    is_active: Mapped[bool] = mapped_column(
        "isActive", 
        Boolean, 
        default=True, 
        nullable=False
    )
    
    # Date de création du compte
    created_at: Mapped[datetime] = mapped_column(
        "createdAt", 
        DateTime(timezone=True), 
        default=utc_now
    )

    # Relations vers les profils spécifiques (un seul sera non-null)
    farmer: Mapped["Farmer | None"] = relationship(back_populates="user", uselist=False)
    expert: Mapped["Expert | None"] = relationship(back_populates="user", uselist=False)
    admin: Mapped["Admin | None"] = relationship(back_populates="user", uselist=False)


class Farmer(Base):
    """
    Profil spécifique pour les agriculteurs.
    
    Les informations géographiques (commune, village, ville) sont
    désormais dans la table Parcelle (relation 1:N).

    Relation 1:1 avec User (même ID).
    """
    __tablename__ = "farmers"

    # Même ID que l'utilisateur parent
    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        ForeignKey("users.id", ondelete="CASCADE"), 
        primary_key=True
    )

    # Relation inverse vers User
    user: Mapped[User] = relationship(back_populates="farmer")

    # Relation vers les parcelles (1:N)
    parcelles: Mapped[list["Parcelle"]] = relationship(
        back_populates="farmer",
        cascade="all, delete-orphan"
    )


class Parcelle(Base):
    """
    Parcelle agricole appartenant à un agriculteur.

    Un agriculteur peut posséder plusieurs parcelles.
    Chaque diagnostic est associé à une parcelle.

    Contient les informations géographiques (commune, village, ville).
    """
    __tablename__ = "parcelles"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4
    )

    # Nom donné par l'agriculteur (ex: "Champ Nord")
    nom_parcelle: Mapped[str] = mapped_column(
        "nomParcelle",
        String(200),
        nullable=False
    )

    # Commune de la parcelle
    commune: Mapped[str] = mapped_column(String(120), default="", nullable=False)

    # Village de la parcelle
    village: Mapped[str] = mapped_column(String(120), default="", nullable=False)

    # Ville la plus proche
    ville: Mapped[str] = mapped_column(String(120), default="", nullable=False)

    # Référence vers l'agriculteur propriétaire
    agriculteur_id: Mapped[uuid.UUID] = mapped_column(
        "agriculteurId",
        UUID(as_uuid=True),
        ForeignKey("farmers.id", ondelete="CASCADE"),
        nullable=False,
        index=True
    )

    # Relations
    farmer: Mapped[Farmer] = relationship(back_populates="parcelles")


class Expert(Base):
    """
    Profil spécifique pour les experts agricoles.
    
    Les experts peuvent valider les diagnostics et créer des alertes.
    """
    __tablename__ = "experts"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        ForeignKey("users.id", ondelete="CASCADE"), 
        primary_key=True
    )
    
    # Domaine d'expertise (ex: "Pathologie végétale", "Agronomie")
    specialite: Mapped[str] = mapped_column(String(120), default="", nullable=False)
    
    # Numéro de matricule professionnel (unique)
    matricule: Mapped[str] = mapped_column(String(80), unique=True, nullable=False)
    
    # Zone d'intervention
    commune: Mapped[str] = mapped_column(String(120), default="", nullable=False)

    user: Mapped[User] = relationship(back_populates="expert")
    alerts: Mapped[list["Alert"]] = relationship(back_populates="expert")


class Admin(Base):
    """
    Profil spécifique pour les administrateurs.
    
    Le niveau d'accès permet de différencier les admins si nécessaire.
    """
    __tablename__ = "admins"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        ForeignKey("users.id", ondelete="CASCADE"), 
        primary_key=True
    )
    
    # Niveau d'accès (1 = standard, 2+ = super admin)
    niveau_acces: Mapped[int] = mapped_column(
        "niveauAcces", 
        Integer, 
        default=1, 
        nullable=False
    )

    user: Mapped[User] = relationship(back_populates="admin")


# ============================================================================
# DIAGNOSTIC ET ANALYSE IA
# ============================================================================

class Location(Base):
    """
    Coordonnées GPS d'un diagnostic.
    
    Permet de localiser géographiquement où la photo a été prise.
    Utile pour les statistiques régionales et les alertes ciblées.
    """
    __tablename__ = "locations"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        primary_key=True, 
        default=uuid.uuid4
    )
    
    # Coordonnées GPS (7 décimales = précision ~1cm)
    latitude: Mapped[float] = mapped_column(Float, nullable=False)
    longitude: Mapped[float] = mapped_column(Float, nullable=False)
    
    # Région administrative
    region: Mapped[str] = mapped_column(String(120), default="", nullable=False)
    
    # Village/localité
    village: Mapped[str] = mapped_column(String(120), default="", nullable=False)


class Diagnostic(Base):
    """
    Un diagnostic effectué par un agriculteur.
    
    C'est l'entité centrale qui relie :
    - L'agriculteur qui l'a effectué
    - L'image analysée
    - La localisation GPS
    - Le résultat de la prédiction IA
    """
    __tablename__ = "diagnostics"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        primary_key=True, 
        default=uuid.uuid4
    )
    
    # Référence vers l'agriculteur
    agriculteur_id: Mapped[uuid.UUID] = mapped_column(
        "agriculteurId", 
        UUID(as_uuid=True), 
        ForeignKey("farmers.id"), 
        nullable=False, 
        index=True
    )

    # Référence vers la parcelle (optionnel pour rétro-compatibilité)
    parcelle_id: Mapped[uuid.UUID | None] = mapped_column(
        "parcelleId",
        UUID(as_uuid=True),
        ForeignKey("parcelles.id", ondelete="SET NULL"),
        nullable=True,
        index=True
    )

    # Date du diagnostic
    date: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=utc_now
    )
    
    # Référence vers la localisation (optionnel)
    location_id: Mapped[uuid.UUID | None] = mapped_column(
        "locationId", 
        UUID(as_uuid=True), 
        ForeignKey("locations.id"), 
        nullable=True
    )
    
    # Référence vers l'image (optionnel, utilise use_alter pour éviter les références circulaires)
    image_id: Mapped[uuid.UUID | None] = mapped_column(
        "imageId",
        UUID(as_uuid=True),
        ForeignKey("images.id", use_alter=True, name="fk_diagnostics_image_id"),
        nullable=True,
    )
    
    # Référence vers la prédiction
    prediction_id: Mapped[uuid.UUID | None] = mapped_column(
        "predictionId",
        UUID(as_uuid=True),
        ForeignKey("prediction_results.id", use_alter=True, name="fk_diagnostics_prediction_id"),
        nullable=True,
    )
    
    # Un expert a-t-il validé ce diagnostic ?
    expert_validated: Mapped[bool] = mapped_column(
        "expertValidated", 
        Boolean, 
        default=False
    )
    
    created_at: Mapped[datetime] = mapped_column(
        "createdAt", 
        DateTime(timezone=True), 
        default=utc_now
    )

    # Relations
    image: Mapped["Image | None"] = relationship(foreign_keys=[image_id], post_update=True)
    location: Mapped[Location | None] = relationship(foreign_keys=[location_id])
    prediction: Mapped["PredictionResult | None"] = relationship(
        foreign_keys=[prediction_id], 
        post_update=True
    )


class Image(Base):
    """
    Métadonnées d'une image de diagnostic.
    
    Le chemin (path) pointe vers le fichier stocké sur le serveur.
    """
    __tablename__ = "images"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        primary_key=True, 
        default=uuid.uuid4
    )
    
    # Chemin du fichier sur le serveur
    path: Mapped[str] = mapped_column(String(500), nullable=False)
    
    # Résolution de l'image (ex: "1920x1080")
    resolution: Mapped[str] = mapped_column(String(50), default="", nullable=False)
    
    # Date de capture
    timestamp: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=utc_now
    )
    
    # Référence vers le diagnostic parent
    diagnostic_id: Mapped[uuid.UUID] = mapped_column(
        "diagnosticId", 
        UUID(as_uuid=True), 
        ForeignKey("diagnostics.id", ondelete="CASCADE"), 
        nullable=False
    )


class PredictionResult(Base):
    """
    Résultat de l'analyse IA d'un diagnostic.
    
    Contient le label prédit (maladie ou "saine") et le score de confiance.
    """
    __tablename__ = "prediction_results"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        primary_key=True, 
        default=uuid.uuid4
    )
    
    # Label prédit par le modèle (ex: "Tomato___Early_blight")
    label: Mapped[str] = mapped_column(String(255), nullable=False)
    
    # Score de confiance (0.0 à 1.0)
    confidence: Mapped[float] = mapped_column(Float, nullable=False)
    
    # Date de la prédiction
    timestamp: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), 
        default=utc_now
    )
    
    # Version du modèle utilisé
    model_version: Mapped[str] = mapped_column(
        "modelVersion", 
        String(60), 
        default="", 
        nullable=False
    )
    
    # Référence vers la maladie correspondante (si mappée)
    maladie_id: Mapped[int | None] = mapped_column(
        "maladieId", 
        ForeignKey("maladies.id", ondelete="SET NULL"), 
        nullable=True
    )
    
    # Référence vers le diagnostic
    diagnostic_id: Mapped[uuid.UUID] = mapped_column(
        "diagnosticId", 
        UUID(as_uuid=True), 
        ForeignKey("diagnostics.id", ondelete="CASCADE"), 
        nullable=False
    )


# ============================================================================
# BASE DE CONNAISSANCES (MALADIES ET TRAITEMENTS)
# ============================================================================

class Maladie(Base):
    """
    Référentiel des maladies de plantes.
    
    Synchronisé vers les mobiles pour la consultation hors-ligne.
    L'ID est fixe (pas auto-incrémenté) pour garantir la cohérence
    avec le mobile.
    """
    __tablename__ = "maladies"

    # ID fixe (correspond aux classes du modèle IA)
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=False)
    
    # Nom courant (ex: "Mildiou de la tomate")
    nom_commun: Mapped[str] = mapped_column(
        "nomCommun", 
        String(150), 
        nullable=False
    )
    
    # Nom scientifique latin
    nom_scientifique: Mapped[str] = mapped_column(
        "nomScientifique", 
        String(150), 
        default="", 
        nullable=False
    )
    
    # Description détaillée de la maladie
    description: Mapped[str] = mapped_column(Text, default="", nullable=False)

    # Relation vers les traitements
    traitements: Mapped[list["Traitement"]] = relationship(
        back_populates="maladie",
        cascade="all, delete-orphan"
    )


class Traitement(Base):
    """
    Traitements recommandés pour une maladie.
    
    Chaque maladie peut avoir plusieurs traitements.
    Synchronisé vers les mobiles avec les maladies.
    """
    __tablename__ = "traitements"

    # ID fixe pour la cohérence mobile
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=False)
    
    # Titre du traitement (ex: "Application de fongicide")
    titre: Mapped[str] = mapped_column(String(200), nullable=False)
    
    # Description détaillée
    description: Mapped[str] = mapped_column(Text, default="", nullable=False)
    
    # Dosage recommandé (ex: "2ml/L d'eau")
    dosage: Mapped[str] = mapped_column(String(200), default="", nullable=False)
    
    # Référence vers la maladie
    maladie_id: Mapped[int] = mapped_column(
        "maladieId", 
        ForeignKey("maladies.id", ondelete="CASCADE")
    )

    maladie: Mapped[Maladie] = relationship(back_populates="traitements")


# ============================================================================
# ALERTES ET MODÈLE IA
# ============================================================================

class Alert(Base):
    """
    Alertes agricoles régionales.
    
    Créées par les experts ou admins pour prévenir les agriculteurs
    d'une situation (épidémie, météo, conseil saisonnier).
    """
    __tablename__ = "alertes"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), 
        primary_key=True, 
        default=uuid.uuid4
    )
    
    # Contenu de l'alerte
    message: Mapped[str] = mapped_column(Text, nullable=False)
    
    # Zone géographique concernée
    zone: Mapped[str] = mapped_column(String(120), default="", nullable=False)
    
    # Niveau de gravité (0.0 à 1.0, où 1.0 = très grave)
    gravite: Mapped[float] = mapped_column(Float, default=0.0, nullable=False)
    
    # Date d'émission
    date_emission: Mapped[datetime] = mapped_column(
        "dateEmission", 
        DateTime(timezone=True), 
        default=utc_now
    )
    
    # Date d'expiration (optionnel)
    date_expiration: Mapped[datetime | None] = mapped_column(
        "dateExpiration", 
        DateTime(timezone=True), 
        nullable=True
    )
    
    # Expert qui a créé l'alerte
    expert_id: Mapped[uuid.UUID | None] = mapped_column(
        "expertId", 
        UUID(as_uuid=True), 
        ForeignKey("experts.id", ondelete="SET NULL"), 
        nullable=True
    )
    
    # Maladie liée (optionnel)
    maladie_id: Mapped[int | None] = mapped_column(
        "maladieId", 
        ForeignKey("maladies.id", ondelete="SET NULL"), 
        nullable=True
    )

    expert: Mapped[Expert | None] = relationship(back_populates="alerts")


class ModeleIA(Base):
    """
    Informations sur les versions du modèle TensorFlow Lite.
    
    Permet de gérer les mises à jour du modèle IA sur les mobiles.
    """
    __tablename__ = "modeles_ia"

    # Version comme clé primaire (ex: "1.0.0", "2.0.0")
    version: Mapped[str] = mapped_column(String(60), primary_key=True)
    
    # Framework utilisé
    framework: Mapped[str] = mapped_column(
        String(50), 
        default="tflite", 
        nullable=False
    )
    
    # Précision du modèle sur le jeu de test (%)
    precision: Mapped[float] = mapped_column(Float, default=0.0, nullable=False)
    
    # Taille d'entrée attendue (224x224 par défaut)
    input_size: Mapped[int] = mapped_column(
        "inputSize", 
        Integer, 
        default=224, 
        nullable=False
    )
    
    # Date de déploiement
    date_deployment: Mapped[datetime] = mapped_column(
        "dateDeployment", 
        DateTime(timezone=True), 
        default=utc_now
    )
