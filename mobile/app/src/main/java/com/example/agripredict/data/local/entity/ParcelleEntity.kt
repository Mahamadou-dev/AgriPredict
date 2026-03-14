package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table : ParcelleLocal
 *
 * Représente une parcelle agricole appartenant à un utilisateur.
 * Un agriculteur peut avoir plusieurs parcelles (relation 1:N).
 *
 * Contient les informations géographiques qui étaient avant
 * dans l'utilisateur : commune, village + nouvelle ville.
 *
 * Chaque diagnostic est associé à une parcelle.
 */
@Entity(
    tableName = "parcelle_local",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["utilisateurId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["utilisateurId"])]
)
data class ParcelleEntity(
    @PrimaryKey
    val id: String,                 // UUID
    val nomParcelle: String,        // Nom donné par l'agriculteur (ex: "Champ Nord")
    val commune: String = "",       // Commune de la parcelle
    val village: String = "",       // Village de la parcelle
    val ville: String = "",         // Ville la plus proche
    val utilisateurId: String       // FK → UtilisateurLocal
)

