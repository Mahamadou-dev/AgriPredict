"""
Schémas Pydantic pour l'authentification.

Ces schémas définissent les structures de données pour:
- L'inscription (RegisterRequest)
- La connexion (LoginRequest)
- Le token JWT (TokenResponse)
"""

from pydantic import BaseModel, Field


class RegisterRequest(BaseModel):
    """
    Données requises pour créer un compte agriculteur.
    
    Inscription simplifiée : nom, téléphone, mot de passe.
    Les parcelles sont ajoutées séparément après l'inscription.
    """
    nomPrenom: str = Field(..., min_length=2, max_length=120, description="Nom et prénom")
    telephone: str = Field(..., min_length=8, max_length=30, description="Numéro de téléphone (unique)")
    password: str = Field(..., min_length=4, description="Mot de passe (4 caractères minimum)")


class LoginRequest(BaseModel):
    """
    Données pour se connecter.
    
    L'utilisateur s'identifie avec son téléphone et mot de passe.
    """
    telephone: str = Field(..., description="Numéro de téléphone")
    password: str = Field(..., description="Mot de passe")


class TokenResponse(BaseModel):
    """
    Réponse après une connexion réussie.
    
    Le token JWT doit être envoyé dans le header Authorization
    pour les requêtes authentifiées:
        Authorization: Bearer <access_token>
    """
    access_token: str = Field(..., description="Token JWT")
    token_type: str = Field(default="bearer", description="Type de token (toujours 'bearer')")
