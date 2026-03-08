package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table : TraitementLocal
 *
 * Stocke les traitements recommandés pour chaque maladie.
 * Synchronisé depuis le serveur (downlink).
 */
@Entity(
    tableName = "traitement_local",
    foreignKeys = [
        ForeignKey(
            entity = MaladieEntity::class,
            parentColumns = ["id"],
            childColumns = ["maladieId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["maladieId"])]
)
data class TraitementEntity(
    @PrimaryKey
    val id: Int,                       // ID entier (correspond au serveur)
    val titre: String,
    val description: String = "",
    val dosage: String = "",
    val maladieId: Int                 // FK → MaladieLocal
)

