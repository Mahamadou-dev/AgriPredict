package com.example.agripredict.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.agripredict.R

/**
 * Écran d'accueil de AgriPredict.
 *
 * Affiche une grille de boutons permettant d'accéder aux fonctionnalités.
 * Design agricole avec couleurs naturelles et icônes claires.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDiagnostic: () -> Unit,
    onNavigateToDiseases: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToExpert: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Liste des boutons du menu principal
    val menuItems = listOf(
        HomeMenuItem(
            titleRes = R.string.home_do_diagnostic,
            icon = Icons.Filled.CameraAlt,
            onClick = onNavigateToDiagnostic
        ),
        HomeMenuItem(
            titleRes = R.string.home_learn_diseases,
            icon = Icons.Filled.Spa,
            onClick = onNavigateToDiseases
        ),
        HomeMenuItem(
            titleRes = R.string.home_alerts,
            icon = Icons.Filled.Notifications,
            onClick = onNavigateToAlerts
        ),
        HomeMenuItem(
            titleRes = R.string.home_history,
            icon = Icons.Filled.History,
            onClick = onNavigateToHistory
        ),
        HomeMenuItem(
            titleRes = R.string.home_contact_expert,
            icon = Icons.Filled.SupportAgent,
            onClick = onNavigateToExpert
        ),
        HomeMenuItem(
            titleRes = R.string.home_about,
            icon = Icons.Filled.Info,
            onClick = onNavigateToAbout
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Bouton paramètres (accès au changement de langue)
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sous-titre de bienvenue
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Grille de boutons du menu principal (2 colonnes)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(menuItems) { item ->
                    HomeMenuCard(item = item)
                }
            }
        }
    }
}

/**
 * Carte du menu principal.
 *
 * Chaque carte représente une fonctionnalité avec icône et texte.
 */
@Composable
private fun HomeMenuCard(item: HomeMenuItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Cartes carrées
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.titleRes),
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(item.titleRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Modèle de données pour un élément du menu d'accueil.
 */
private data class HomeMenuItem(
    val titleRes: Int,            // ID de la ressource string (i18n)
    val icon: ImageVector,        // Icône Material
    val onClick: () -> Unit       // Action au clic
)

