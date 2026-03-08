package com.example.agripredict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.agripredict.ui.navigation.AgriPredictNavGraph
import com.example.agripredict.ui.theme.AgriPredictTheme

/**
 * Activité principale de AgriPredict.
 *
 * Point d'entrée de l'application.
 * Initialise le thème Material 3 et le graphe de navigation Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgriPredictTheme {
                // Crée le contrôleur de navigation
                val navController = rememberNavController()
                // Lance le graphe de navigation (écran d'accueil par défaut)
                AgriPredictNavGraph(navController = navController)
            }
        }
    }
}
