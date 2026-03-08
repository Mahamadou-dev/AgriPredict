package com.example.agripredict.domain.model

import com.example.agripredict.sync.SyncStatus

/**
 * Modèle métier représentant un diagnostic complet.
 *
 * Combine les données de DiagnosticLocal, PredictionLocal,
 * ImageLocal et LocationLocal en un seul objet pour la couche UI.
 */
data class DiagnosticResult(
    val id: String,
    val userId: String,
    val date: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    // Prédiction IA
    val label: String = "",            // Code maladie détecté (ex: "MILDEW")
    val confidence: Float = 0f,        // 0.0 à 1.0
    val modelVersion: String = "",
    // Image
    val imagePath: String = "",
    // Localisation
    val latitude: Double? = null,
    val longitude: Double? = null,
    val region: String = ""
)

