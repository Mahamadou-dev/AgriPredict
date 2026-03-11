package com.example.agripredict.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agripredict.R

/**
 * Écran d'accueil de AgriPredict — Design moderne agricole.
 *
 * Structure :
 * - TopAppBar : 👤 Profil (gauche) — Titre — ⚙️ Paramètres (droite)
 * - Bannière de bienvenue avec gradient vert
 * - Grille 2×3 de cartes avec icônes colorées individuelles
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
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    userName: String = ""
) {
    // Chaque carte a sa propre couleur d'accent pour un look moderne
    val menuItems = listOf(
        HomeMenuItem(
            titleRes = R.string.home_do_diagnostic,
            icon = Icons.Filled.CameraAlt,
            onClick = onNavigateToDiagnostic,
            iconTint = Color(0xFF2E7D32),
            bgColor = Color(0xFFE8F5E9)
        ),
        HomeMenuItem(
            titleRes = R.string.home_learn_diseases,
            icon = Icons.Filled.Spa,
            onClick = onNavigateToDiseases,
            iconTint = Color(0xFFE65100),
            bgColor = Color(0xFFFFF3E0)
        ),
        HomeMenuItem(
            titleRes = R.string.home_alerts,
            icon = Icons.Filled.Notifications,
            onClick = onNavigateToAlerts,
            iconTint = Color(0xFFC62828),
            bgColor = Color(0xFFFFEBEE)
        ),
        HomeMenuItem(
            titleRes = R.string.home_history,
            icon = Icons.Filled.History,
            onClick = onNavigateToHistory,
            iconTint = Color(0xFF1565C0),
            bgColor = Color(0xFFE3F2FD)
        ),
        HomeMenuItem(
            titleRes = R.string.home_contact_expert,
            icon = Icons.Filled.SupportAgent,
            onClick = onNavigateToExpert,
            iconTint = Color(0xFF6A1B9A),
            bgColor = Color(0xFFF3E5F5)
        ),
        HomeMenuItem(
            titleRes = R.string.home_about,
            icon = Icons.Filled.Info,
            onClick = onNavigateToAbout,
            iconTint = Color(0xFF4E342E),
            bgColor = Color(0xFFEFEBE9)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Spa,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                // 👤 Profil à GAUCHE
                navigationIcon = {
                    IconButton(onClick = onNavigateToProfile) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = stringResource(R.string.profile_title),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                },
                // ⚙️ Paramètres à DROITE
                actions = {
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
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        // Animation d'apparition
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // === Bannière de bienvenue avec gradient vert — avec animation ===
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(400))
            ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (userName.isNotEmpty()) {
                                    stringResource(R.string.home_welcome_user, userName)
                                } else {
                                    stringResource(R.string.home_subtitle)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Agriculture,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === Grille de boutons du menu principal (2 colonnes) ===
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
 * Carte du menu principal avec icône colorée unique.
 * Chaque carte a sa propre couleur pour faciliter la reconnaissance visuelle.
 */
@Composable
private fun HomeMenuCard(item: HomeMenuItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icône dans un cercle coloré individuel
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = item.bgColor
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = stringResource(item.titleRes),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    tint = item.iconTint
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(item.titleRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Modèle de données pour un élément du menu d'accueil.
 */
private data class HomeMenuItem(
    val titleRes: Int,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val iconTint: Color = Color(0xFF2E7D32),
    val bgColor: Color = Color(0xFFE8F5E9)
)
