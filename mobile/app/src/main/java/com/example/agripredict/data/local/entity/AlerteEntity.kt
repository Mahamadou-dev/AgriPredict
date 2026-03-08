package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.agripredict.sync.SyncStatus

/**
 * Table : AlerteLocal
 *
 * Stocke les alertes agricoles (météo, épidémies, conseils).
 * Synchronisées depuis le serveur (downlink).
 */
@Entity(
    tableName = "alerte_local",
    foreignKeys = [
        ForeignKey(
            entity = MaladieEntity::class,
            parentColumns = ["id"],
            childColumns = ["maladieId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["maladieId"]),
        Index(value = ["syncStatus"])
    ]
)
data class AlerteEntity(
    @PrimaryKey
    val id: String,                                     // UUID
    val message: String,
    val zone: String = "",
    val gravite: Float = 0f,
    val dateEmission: Long = System.currentTimeMillis(),
    val dateExpiration: Long? = null,
    val maladieId: Int? = null,                         // FK → MaladieLocal (optionnel)
    val syncStatus: SyncStatus = SyncStatus.PENDING     // PENDING / SYNCED / FAILED
)

