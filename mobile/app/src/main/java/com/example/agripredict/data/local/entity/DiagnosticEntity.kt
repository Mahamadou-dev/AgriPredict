package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.agripredict.sync.SyncStatus

/**
 * Table : DiagnosticLocal
 *
 * Enregistre chaque diagnostic effectué par l'agriculteur.
 * Contient les références vers l'image, la localisation, la prédiction et la parcelle.
 * Le champ syncStatus gère la synchronisation offline-first.
 */
@Entity(
    tableName = "diagnostic_local",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParcelleEntity::class,
            parentColumns = ["id"],
            childColumns = ["parcelleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["syncStatus"]),
        Index(value = ["parcelleId"])
    ]
)
data class DiagnosticEntity(
    @PrimaryKey
    val id: String,                                     // UUID
    val userId: String,                                 // FK → UtilisateurLocal
    val date: Long = System.currentTimeMillis(),        // Date du diagnostic
    val syncStatus: SyncStatus = SyncStatus.PENDING,    // PENDING / SYNCED / FAILED
    val parcelleId: String? = null,                     // FK → ParcelleLocal
    val locationId: String? = null,                     // FK → LocationLocal (optionnel)
    val imageId: String? = null,                        // FK → ImageLocal (optionnel)
    val predictionId: String? = null,                   // FK → PredictionLocal (optionnel)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

