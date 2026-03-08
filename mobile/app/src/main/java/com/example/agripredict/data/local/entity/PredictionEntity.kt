package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table : PredictionLocal
 *
 * Stocke le résultat de l'analyse IA pour un diagnostic.
 */
@Entity(
    tableName = "prediction_local",
    foreignKeys = [
        ForeignKey(
            entity = DiagnosticEntity::class,
            parentColumns = ["id"],
            childColumns = ["diagnosticId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MaladieEntity::class,
            parentColumns = ["id"],
            childColumns = ["maladieId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["diagnosticId"]),
        Index(value = ["maladieId"])
    ]
)
data class PredictionEntity(
    @PrimaryKey
    val id: String,                                     // UUID
    val label: String,                                  // Code maladie détecté (ex: "MILDEW")
    val confidence: Float,                              // Score de confiance (0.0 à 1.0)
    val timestamp: Long = System.currentTimeMillis(),
    val modelVersion: String = "",                      // Version du modèle TFLite utilisé
    val diagnosticId: String,                           // FK → DiagnosticLocal
    val maladieId: Int? = null                          // FK → MaladieLocal (optionnel)
)

