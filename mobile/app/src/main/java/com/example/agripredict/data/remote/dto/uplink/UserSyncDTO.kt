package com.example.agripredict.data.remote.dto.uplink

import kotlinx.serialization.Serializable

/**
 * DTO pour synchroniser le profil utilisateur vers le serveur (UPLINK).
 *
 * Correspond à la table UtilisateurLocal.
 * Ne contient que les champs nécessaires côté serveur.
 */
@Serializable
data class UserSyncDTO(
    val id: String,
    val nom: String,
    val telephone: String,
    val email: String = "",
    val role: String = "",
    val commune: String = "",
    val village: String = ""
)

