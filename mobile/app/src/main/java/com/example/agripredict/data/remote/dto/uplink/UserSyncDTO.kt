package com.example.agripredict.data.remote.dto.uplink

import kotlinx.serialization.Serializable

/**
 * DTO pour synchroniser le profil utilisateur vers le serveur (UPLINK).
 *
 * Contient l'identité de l'agriculteur + ses parcelles.
 * Les informations géographiques sont maintenant dans les parcelles.
 */
@Serializable
data class UserSyncDTO(
    val id: String,
    val nomPrenom: String,
    val telephone: String,
    val parcelles: List<ParcelleSyncDTO> = emptyList()
)

/**
 * DTO pour synchroniser une parcelle vers le serveur (UPLINK).
 */
@Serializable
data class ParcelleSyncDTO(
    val id: String,
    val nomParcelle: String,
    val commune: String = "",
    val village: String = "",
    val ville: String = ""
)

