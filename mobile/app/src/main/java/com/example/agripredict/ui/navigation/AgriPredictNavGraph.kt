package com.example.agripredict.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.agripredict.ui.screens.about.AboutScreen
import com.example.agripredict.ui.screens.alerts.AlertsScreen
import com.example.agripredict.ui.screens.diagnostic.DiagnosticScreen
import com.example.agripredict.ui.screens.diseases.DiseasesScreen
import com.example.agripredict.ui.screens.expert.ExpertScreen
import com.example.agripredict.ui.screens.history.HistoryScreen
import com.example.agripredict.ui.screens.home.HomeScreen
import com.example.agripredict.ui.screens.settings.SettingsScreen

/**
 * Graphe de navigation principal de l'application.
 *
 * Définit toutes les destinations et les transitions entre écrans.
 * L'écran d'accueil (Home) est la destination de départ.
 */
@Composable
fun AgriPredictNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Écran d'accueil
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDiagnostic = { navController.navigate(Screen.Diagnostic.route) },
                onNavigateToDiseases = { navController.navigate(Screen.Diseases.route) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToExpert = { navController.navigate(Screen.Expert.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // Diagnostic IA
        composable(Screen.Diagnostic.route) {
            DiagnosticScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Base de connaissances : maladies
        composable(Screen.Diseases.route) {
            DiseasesScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Alertes agricoles
        composable(Screen.Alerts.route) {
            AlertsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Historique des diagnostics
        composable(Screen.History.route) {
            HistoryScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Contact expert
        composable(Screen.Expert.route) {
            ExpertScreen(onNavigateBack = { navController.popBackStack() })
        }

        // À propos
        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Paramètres
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

