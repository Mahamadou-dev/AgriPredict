package com.example.agripredict.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.agripredict.AgriPredictApplication
import com.example.agripredict.ui.screens.about.AboutScreen
import com.example.agripredict.ui.screens.alerts.AlertsScreen
import com.example.agripredict.ui.screens.alerts.AlertsViewModel
import com.example.agripredict.ui.screens.auth.AuthUiState
import com.example.agripredict.ui.screens.auth.AuthViewModel
import com.example.agripredict.ui.screens.auth.LoginScreen
import com.example.agripredict.ui.screens.auth.ProfileScreen
import com.example.agripredict.ui.screens.auth.RegisterScreen
import com.example.agripredict.ui.screens.diagnostic.DiagnosticScreen
import com.example.agripredict.ui.screens.diagnostic.DiagnosticViewModel
import com.example.agripredict.ui.screens.diseases.DiseasesScreen
import com.example.agripredict.ui.screens.diseases.DiseasesViewModel
import com.example.agripredict.ui.screens.expert.ExpertScreen
import com.example.agripredict.ui.screens.history.HistoryDetailScreen
import com.example.agripredict.ui.screens.history.HistoryScreen
import com.example.agripredict.ui.screens.history.HistoryViewModel
import com.example.agripredict.ui.screens.home.HomeScreen
import com.example.agripredict.ui.screens.settings.SettingsScreen

/**
 * Graphe de navigation principal de l'application.
 *
 * Gère la navigation conditionnelle :
 * - Si l'utilisateur n'est pas connecté → écran Login
 * - Si connecté → écran Home
 *
 * Le AuthViewModel est partagé entre tous les écrans d'auth.
 */
@Composable
fun AgriPredictNavGraph(navController: NavHostController) {
    // Récupérer le conteneur DI depuis l'Application
    val context = LocalContext.current
    val appContainer = (context.applicationContext as AgriPredictApplication).container

    // AuthViewModel partagé au niveau du NavGraph
    val authViewModel: AuthViewModel = viewModel(
        factory = appContainer.authViewModelFactory
    )

    // Observer l'état d'authentification
    val authState by authViewModel.authState.collectAsState()

    // Déterminer la destination de départ selon la session
    when (authState) {
        is AuthUiState.Loading -> {
            // Écran de chargement pendant la vérification de session
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        else -> {
            // Déterminer la destination de départ
            val startDestination = when (authState) {
                is AuthUiState.LoggedIn -> Screen.Home.route
                else -> Screen.Login.route
            }

            // Nom d'utilisateur pour l'accueil
            val userName = when (authState) {
                is AuthUiState.LoggedIn -> (authState as AuthUiState.LoggedIn).userName
                else -> ""
            }

            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(350)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(350)
                    ) + fadeOut(animationSpec = tween(200))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(350)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350)
                    ) + fadeOut(animationSpec = tween(200))
                }
            ) {
                // ==========================================
                // ÉCRANS D'AUTHENTIFICATION
                // ==========================================

                // Connexion
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = {
                            navController.navigate(Screen.Register.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Inscription
                composable(Screen.Register.route) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        },
                        onRegistrationSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Profil
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // ==========================================
                // ÉCRANS PRINCIPAUX
                // ==========================================

                // Écran d'accueil
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToDiagnostic = { navController.navigate(Screen.Diagnostic.route) },
                        onNavigateToDiseases = { navController.navigate(Screen.Diseases.route) },
                        onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToExpert = { navController.navigate(Screen.Expert.route) },
                        onNavigateToAbout = { navController.navigate(Screen.About.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        userName = userName
                    )
                }

                // Diagnostic IA — avec ViewModel injecté
                composable(Screen.Diagnostic.route) {
                    val diagnosticViewModel: DiagnosticViewModel = viewModel(
                        factory = appContainer.diagnosticViewModelFactory
                    )
                    DiagnosticScreen(
                        viewModel = diagnosticViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Base de connaissances : maladies
                composable(Screen.Diseases.route) {
                    val diseasesViewModel: DiseasesViewModel = viewModel(
                        factory = appContainer.diseasesViewModelFactory
                    )
                    DiseasesScreen(
                        viewModel = diseasesViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Alertes agricoles
                composable(Screen.Alerts.route) {
                    val alertsViewModel: AlertsViewModel = viewModel(
                        factory = appContainer.alertsViewModelFactory
                    )
                    AlertsScreen(
                        viewModel = alertsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Historique des diagnostics
                composable(Screen.History.route) {
                    val historyViewModel: HistoryViewModel = viewModel(
                        factory = appContainer.historyViewModelFactory
                    )
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDetail = { diagnosticId ->
                            navController.navigate(Screen.HistoryDetail.createRoute(diagnosticId))
                        }
                    )
                }

                // Détail d'un diagnostic
                composable(
                    route = Screen.HistoryDetail.route,
                    arguments = listOf(navArgument("diagnosticId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val diagnosticId = backStackEntry.arguments?.getString("diagnosticId") ?: ""
                    val historyViewModel: HistoryViewModel = viewModel(
                        factory = appContainer.historyViewModelFactory
                    )
                    HistoryDetailScreen(
                        viewModel = historyViewModel,
                        diagnosticId = diagnosticId,
                        onNavigateBack = { navController.popBackStack() }
                    )
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
    }
}
