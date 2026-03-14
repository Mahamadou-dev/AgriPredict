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

    /** Écran de connexion */
    data object Login : Screen("login")

    /** Écran d'inscription */
    data object Register : Screen("register")

    /** Écran de profil utilisateur */
    data object Profile : Screen("profile")

    /** Écran de gestion des parcelles */
    data object Parcelles : Screen("parcelles")

    /** Écran d'ajout d'une parcelle */
    data object AddParcelle : Screen("add_parcelle")

    /** Écran d'édition d'une parcelle */
    data object EditParcelle : Screen("edit_parcelle/{parcelleId}") {
        /** Crée la route avec l'ID de la parcelle */
        fun createRoute(parcelleId: String) = "edit_parcelle/$parcelleId"
    }

    /** Écran de détail d'un diagnostic */
    data object HistoryDetail : Screen("history_detail/{diagnosticId}") {
        /** Crée la route avec l'ID du diagnostic */
        fun createRoute(diagnosticId: String) = "history_detail/$diagnosticId"
    }
}
