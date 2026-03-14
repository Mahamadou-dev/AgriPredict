package com.example.agripredict.ui.screens.parcelle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.agripredict.R
import com.example.agripredict.data.local.entity.ParcelleEntity

/**
 * Écran de gestion des parcelles agricoles.
 *
 * Affiche la liste des parcelles de l'agriculteur connecté.
 * Permet d'ajouter (FAB) ou de supprimer (swipe/dialog) une parcelle.
 *
 * Accessible depuis :
 * - Le bouton "Mes parcelles" sur l'accueil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelleScreen(
    viewModel: ParcelleViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var parcelleToDelete by remember { mutableStateOf<ParcelleEntity?>(null) }

    // Dialog de confirmation de suppression
    parcelleToDelete?.let { parcelle ->
        AlertDialog(
            onDismissRequest = { parcelleToDelete = null },
            title = { Text(stringResource(R.string.parcelle_delete_title)) },
            text = {
                Text(
                    stringResource(R.string.parcelle_delete_confirm, parcelle.nomParcelle)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteParcelle(parcelle)
                        parcelleToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { parcelleToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.parcelle_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.parcelle_add))
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ParcelleUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ParcelleUiState.Empty -> {
                // État vide — encourager l'ajout
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Terrain,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.parcelle_empty_list),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToAdd,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.parcelle_add))
                        }
                    }
                }
            }

            is ParcelleUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.parcelles, key = { it.id }) { parcelle ->
                        ParcelleCard(
                            parcelle = parcelle,
                            onDelete = { parcelleToDelete = parcelle }
                        )
                    }
                }
            }

            is ParcelleUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/**
 * Carte affichant une parcelle avec ses informations et un bouton supprimer.
 */
@Composable
private fun ParcelleCard(
    parcelle: ParcelleEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône parcelle
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Filled.Terrain,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            // Informations
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parcelle.nomParcelle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                val locationParts = listOfNotNull(
                    parcelle.village.ifEmpty { null },
                    parcelle.commune.ifEmpty { null },
                    parcelle.ville.ifEmpty { null }
                )
                if (locationParts.isNotEmpty()) {
                    Text(
                        text = locationParts.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bouton supprimer
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

