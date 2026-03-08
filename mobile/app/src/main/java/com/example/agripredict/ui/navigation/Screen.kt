package com.example.agripredict.ui.navigation

/**
 * Définition des routes de navigation de l'application.
 *
 * Chaque écran a une route unique utilisée par Navigation Compose.
 * Simple et clair — une sealed class avec des objets.
 */
sealed class Screen(val route: String) {
    /** Écran d'accueil */
    data object Home : Screen("home")

    /** Écran de diagnostic IA */
    data object Diagnostic : Screen("diagnostic")

    /** Écran des maladies (base de connaissances) */
    data object Diseases : Screen("diseases")

    /** Écran des alertes agricoles */
    data object Alerts : Screen("alerts")

    /** Écran historique des diagnostics */
    data object History : Screen("history")

    /** Écran contact expert */
    data object Expert : Screen("expert")

    /** Écran à propos */
    data object About : Screen("about")

    /** Écran paramètres (langue, etc.) */
    data object Settings : Screen("settings")
}

