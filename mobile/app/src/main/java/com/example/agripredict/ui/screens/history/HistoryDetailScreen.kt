package com.example.agripredict.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.agripredict.R
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.model.Maladie
import com.example.agripredict.sync.SyncStatus
import com.example.agripredict.ui.components.ConfidenceBar
import com.example.agripredict.ui.components.StatusBadge
import com.example.agripredict.ui.components.TraitementCard
import com.example.agripredict.util.LabelFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran de détail d'un diagnostic.
 * Affiche l'image, le résultat, les traitements recommandés.
 * Design identique à la page résultat diagnostic pour cohérence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryViewModel,
    diagnosticId: String,
    onNavigateBack: () -> Unit
) {
    val diagnostic by viewModel.selectedDiagnostic.collectAsState()
    val maladie by viewModel.selectedMaladie.collectAsState()

    LaunchedEffect(diagnosticId) {
        viewModel.loadDiagnosticById(diagnosticId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_detail_title)) },
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
        }
    ) { innerPadding ->
        val diag = diagnostic
        if (diag == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            HistoryDetailContent(
                diagnostic = diag,
                maladie = maladie,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HistoryDetailContent(
    diagnostic: DiagnosticResult,
    maladie: Maladie?,
    modifier: Modifier = Modifier
) {
    val formatted = LabelFormatter.format(diagnostic.label)
    val dateStr = remember(diagnostic.date) {
        SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.getDefault()).format(Date(diagnostic.date))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // === Image section ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.HideImage,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.history_no_image),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Badge plante
            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Eco, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        formatted.plant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // === Carte résultat principal (identique au diagnostic) ===
            val statusColor = if (formatted.isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.08f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StatusBadge(
                        isHealthy = formatted.isHealthy,
                        healthyText = stringResource(R.string.diagnostic_result_healthy),
                        diseaseText = stringResource(R.string.diagnostic_result_disease)
                    )

                    if (!formatted.isHealthy && formatted.disease.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = formatted.disease,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = statusColor.copy(alpha = 0.2f))
                    Spacer(Modifier.height(12.dp))

                    ConfidenceBar(
                        confidence = diagnostic.confidence,
                        label = stringResource(R.string.diagnostic_result_confidence)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Section Traitements Recommandés ===
            AnimatedVisibility(
                visible = maladie != null,
                enter = expandVertically(animationSpec = tween(500)) + fadeIn(tween(400, delayMillis = 100)),
                exit = shrinkVertically() + fadeOut()
            ) {
                maladie?.let { m ->
                    Column {
                        // Description de la maladie
                        if (m.description.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(R.string.diagnostic_disease_info),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = m.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Traitements
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.MedicalServices,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (formatted.isHealthy) stringResource(R.string.diagnostic_healthy_advice)
                                    else stringResource(R.string.diagnostic_treatment_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.diagnostic_treatment_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (m.traitements.isNotEmpty()) {
                            m.traitements.forEach { traitement ->
                                TraitementCard(
                                    titre = traitement.titre,
                                    description = traitement.description,
                                    dosage = traitement.dosage,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Informations détaillées ===
            Text(
                text = stringResource(R.string.history_detail_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailInfoRow(
                        icon = Icons.Filled.CalendarMonth,
                        label = stringResource(R.string.history_date),
                        value = dateStr
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    DetailInfoRow(
                        icon = Icons.Filled.Memory,
                        label = stringResource(R.string.history_detail_model_version),
                        value = diagnostic.modelVersion.ifEmpty { "MobileNetV2 INT8" }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    val syncLabel = when (diagnostic.syncStatus) {
                        SyncStatus.SYNCED -> "✅ ${stringResource(R.string.sync_success)}"
                        SyncStatus.PENDING -> "⏳ ${stringResource(R.string.sync_offline)}"
                        SyncStatus.FAILED -> "❌ ${stringResource(R.string.sync_error)}"
                    }
                    DetailInfoRow(
                        icon = Icons.Filled.CloudSync,
                        label = stringResource(R.string.history_detail_sync_status),
                        value = syncLabel
                    )

                    if (diagnostic.region.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        DetailInfoRow(
                            icon = Icons.Filled.LocationOn,
                            label = "Région",
                            value = diagnostic.region
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
