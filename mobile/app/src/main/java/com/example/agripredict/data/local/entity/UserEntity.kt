package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table : UtilisateurLocal
 *
 * Stocke le profil de l'agriculteur en local.
 * Simplifié par rapport au serveur — le mobile n'a pas besoin
 * de toute la gestion des comptes.
 */
@Entity(tableName = "utilisateur_local")
data class UserEntity(
    @PrimaryKey
    val id: String,                 // UUID
    val nom: String,
    val telephone: String,
    val email: String = "",
    val role: String = "",          // ex: "agriculteur", "technicien"
    val commune: String = "",
    val village: String = "",
    val isActive: Boolean = true,
    val lastLogin: Long? = null     // Timestamp du dernier login
)

