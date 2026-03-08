package com.example.agripredict.data.remote.dto.downlink

import kotlinx.serialization.Serializable

/**
 * Réponse du serveur lors de la vérification des mises à jour (DOWNLINK).
 */
@Serializable
data class CheckUpdatesResponseDTO(
    val hasUpdate: Boolean,
    val knowledgeBaseVersion: String = "",
    val modelVersion: String = "",
    val appConfigVersion: String = ""
)

/**
 * DTO pour la base de connaissances — maladies et traitements (DOWNLINK).
 */
@Serializable
data class KnowledgeBaseDTO(
    val version: String,
    val maladies: List<MaladieDTO> = emptyList(),
    val traitements: List<TraitementDTO> = emptyList()
)

@Serializable
data class MaladieDTO(
    val id: Int,
    val nomCommun: String,
    val nomScientifique: String = "",
    val description: String = ""
)

@Serializable
data class TraitementDTO(
    val id: Int,
    val titre: String,
    val description: String = "",
    val dosage: String = "",
    val maladieId: Int
)

/**
 * DTO pour les alertes reçues du serveur (DOWNLINK).
 */
@Serializable
data class AlerteDTO(
    val id: String,
    val message: String,
    val zone: String = "",
    val gravite: Float = 0f,
    val dateEmission: Long,
    val dateExpiration: Long? = null,
    val maladieId: Int? = null
)

/**
 * DTO pour la mise à jour du modèle IA (DOWNLINK).
 */
@Serializable
data class ModelUpdateDTO(
    val version: String,
    val downloadUrl: String,
    val framework: String = "tflite",
    val precision: Float = 0f,
    val inputSize: Int = 224,
    val checksum: String = ""
)

/**
 * DTO pour la configuration de l'application (DOWNLINK).
 */
@Serializable
data class AppConfigDTO(
    val version: String,
    val syncIntervalMinutes: Int = 60,
    val maxImageSizeMb: Int = 5,
    val features: Map<String, Boolean> = emptyMap()
)

/**
 * DTO regroupant toutes les mises à jour disponibles (DOWNLINK).
 *
 * Le mobile reçoit ce bundle et applique les mises à jour localement.
 */
@Serializable
data class UpdateBundleDTO(
    val knowledgeBase: KnowledgeBaseDTO? = null,
    val alertes: List<AlerteDTO> = emptyList(),
    val modelUpdate: ModelUpdateDTO? = null,
    val appConfig: AppConfigDTO? = null
)

