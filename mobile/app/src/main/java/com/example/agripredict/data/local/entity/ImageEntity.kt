package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table : ImageLocal
 *
 * Stocke les métadonnées de chaque image capturée pour un diagnostic.
 */
@Entity(
    tableName = "image_local",
    foreignKeys = [
        ForeignKey(
            entity = DiagnosticEntity::class,
            parentColumns = ["id"],
            childColumns = ["diagnosticId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["diagnosticId"])]
)
data class ImageEntity(
    @PrimaryKey
    val id: String,                                     // UUID
    val path: String,                                   // Chemin fichier local
    val resolution: String = "",                        // ex: "1920x1080"
    val timestamp: Long = System.currentTimeMillis(),
    val diagnosticId: String                            // FK → DiagnosticLocal
)

