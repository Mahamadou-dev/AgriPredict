package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table : MaladieLocal
 *
 * Base de connaissances des maladies de plantes, stockée localement.
 * Synchronisée depuis le serveur (downlink).
 * Les textes sont stockés directement car ils proviennent du serveur.
 */
@Entity(tableName = "maladie_local")
data class MaladieEntity(
    @PrimaryKey
    val id: Int,                       // ID entier (correspond au serveur)
    val nomCommun: String,             // Nom courant de la maladie
    val nomScientifique: String = "",  // Nom scientifique latin
    val description: String = ""       // Description détaillée
)

