package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table : ModeleIALocal
 *
 * Stocke les informations sur le modèle TensorFlow Lite installé.
 * Permet de vérifier si une mise à jour du modèle est disponible.
 */
@Entity(tableName = "modele_ia_local")
data class ModeleIAEntity(
    @PrimaryKey
    val version: String,               // Version du modèle (ex: "1.0.0")
    val framework: String = "tflite",  // Framework utilisé (ex: "tflite")
    val modelPath: String = "",        // Chemin du fichier modèle sur l'appareil
    val precision: Float = 0f,         // Précision du modèle (%)
    val inputSize: Int = 224,          // Taille d'entrée attendue (px)
    val installedAt: Long = System.currentTimeMillis()
)

