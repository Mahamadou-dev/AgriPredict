package com.example.agripredict.data.remote.dto.uplink

import kotlinx.serialization.Serializable

/**
 * DTO pour envoyer un diagnostic complet au serveur (UPLINK).
 *
 * Regroupe les données du diagnostic, de l'image, de la localisation
 * et de la prédiction en un seul objet pour l'envoi.
 */
@Serializable
data class DiagnosticUploadDTO(
    val id: String,
    val userId: String,
    val date: Long,
    val location: LocationUploadDTO? = null,
    val prediction: PredictionUploadDTO? = null
)

@Serializable
data class LocationUploadDTO(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val region: String = "",
    val village: String = ""
)

@Serializable
data class PredictionUploadDTO(
    val id: String,
    val label: String,
    val confidence: Float,
    val modelVersion: String = "",
    val maladieId: Int? = null
)

/**
 * Réponse du serveur après l'envoi d'un diagnostic.
 */
@Serializable
data class DiagnosticUploadResponseDTO(
    val success: Boolean,
    val diagnosticId: String,
    val message: String = ""
)

