package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table : UtilisateurLocal
 *
 * Stocke le profil de l'agriculteur en local.
 * Simplifié — ne contient que l'identité et l'authentification.
 *
 * Les informations géographiques (commune, village, ville)
 * sont désormais dans ParcelleEntity (relation 1:N).
 */
@Entity(tableName = "utilisateur_local")
data class UserEntity(
    @PrimaryKey
    val id: String,                                     // UUID
    val nomPrenom: String,                              // Nom et prénom
    val telephone: String,                              // Numéro unique (login)
    val motDePasseHash: String = "123456",              // Mot de passe (défaut : 123456)
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()    // Date de création
)

