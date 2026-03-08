package com.example.agripredict.domain.model

/**
 * Modèle métier représentant une maladie de plante.
 *
 * Correspond à la table MaladieLocal + ses traitements associés.
 */
data class Maladie(
    val id: Int,
    val nomCommun: String,
    val nomScientifique: String = "",
    val description: String = "",
    val traitements: List<Traitement> = emptyList()
)

/**
 * Modèle métier représentant un traitement recommandé.
 *
 * Correspond à la table TraitementLocal.
 */
data class Traitement(
    val id: Int,
    val titre: String,
    val description: String = "",
    val dosage: String = "",
    val maladieId: Int
)

