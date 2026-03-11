package com.example.agripredict.ui.screens.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.agripredict.R
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.util.LabelFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran de l'historique des diagnostics.
 * Affiche la liste des diagnostics réels depuis Room via HistoryViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<DiagnosticResult?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteSuccessMsg = stringResource(R.string.history_delete_success)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text(stringResource(R.string.history_search)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is HistoryUiState.Empty -> {
                    HistoryEmptyState()
                }

                is HistoryUiState.Error -> {
                    HistoryErrorState(
                        message = state.message,
                        onRetry = { /* ViewModel reloads automatically via Flow */ }
                    )
                }

                is HistoryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.diagnostics,
                            key = { it.id }
                        ) { diagnostic ->
                            HistoryDiagnosticCard(
                                diagnostic = diagnostic,
                                onClick = { onNavigateToDetail(diagnostic.id) },
                                onDelete = { showDeleteDialog = diagnostic }
                            )
                        }
                        // Espace en bas
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }

    // Dialog de confirmation de suppression
    showDeleteDialog?.let { diagnostic ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.history_delete)) },
            text = { Text(stringResource(R.string.history_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDiagnostic(diagnostic.id)
                        showDeleteDialog = null
                        // Show snackbar via LaunchedEffect would be ideal but simple approach:
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.history_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Carte d'un diagnostic dans l'historique.
 */
@Composable
private fun HistoryDiagnosticCard(
    diagnostic: DiagnosticResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formatted = LabelFormatter.format(diagnostic.label)
    val dateStr = remember(diagnostic.date) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(diagnostic.date))
    }
    val confidencePercent = (diagnostic.confidence * 100).toInt()

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (diagnostic.imagePath.isNotEmpty() && File(diagnostic.imagePath).exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(diagnostic.imagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.history_detail_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = stringResource(R.string.history_no_image),
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                // Plante
                Text(
                    text = formatted.plant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Maladie ou Saine
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = if (formatted.isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (formatted.isHealthy) stringResource(R.string.history_healthy) else formatted.disease,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (formatted.isHealthy) Color(0xFF388E3C) else Color(0xFFD32F2F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date + Confiance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    // Badge confiance
                    val confColor = when {
                        confidencePercent >= 80 -> Color(0xFF4CAF50)
                        confidencePercent >= 50 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = confColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$confidencePercent%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = confColor
                        )
                    }
                }
            }

            // Bouton supprimer
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.history_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * État vide — aucun diagnostic enregistré.
 */
@Composable
private fun HistoryEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.history_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.history_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * État erreur.
 */
@Composable
private fun HistoryErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.retry))
        }
    }
}

