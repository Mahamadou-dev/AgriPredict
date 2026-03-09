package com.example.agripredict.util

/**
 * Utilitaire pour formater les labels bruts du modèle IA
 * en texte lisible par l'utilisateur.
 *
 * Exemple :
 *   "Tomato___Late_blight" → plante="Tomato", maladie="Late blight"
 *   "cassava___healthy"    → plante="Cassava", maladie="Healthy"
 */
object LabelFormatter {

    /**
     * Résultat d'un label formaté.
     *
     * @param plant Nom de la plante (ex: "Tomate")
     * @param disease Nom de la maladie (ex: "Late blight") ou "Saine" si healthy
     * @param isHealthy True si la plante est saine
     */
    data class FormattedLabel(
        val plant: String,
        val disease: String,
        val isHealthy: Boolean
    )

    /**
     * Formate un label brut du modèle en texte lisible.
     *
     * Format attendu : "Plante___Maladie" (séparé par ___)
     */
    fun format(rawLabel: String): FormattedLabel {
        // Séparer plante et maladie par "___"
        val parts = rawLabel.split("___", limit = 2)

        val rawPlant = if (parts.isNotEmpty()) parts[0] else rawLabel
        val rawDisease = if (parts.size > 1) parts[1] else ""

        // Nettoyer les noms
        val plant = cleanName(rawPlant)
        val disease = cleanName(rawDisease)
        val isHealthy = rawDisease.lowercase().contains("healthy")

        return FormattedLabel(
            plant = plant,
            disease = if (isHealthy) "" else disease,
            isHealthy = isHealthy
        )
    }

    /**
     * Nettoie un nom brut :
     * - Remplace les _ par des espaces
     * - Supprime les parenthèses inutiles
     * - Met la première lettre en majuscule
     */
    private fun cleanName(raw: String): String {
        return raw
            .replace("_", " ")
            .replace("  ", " ")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

