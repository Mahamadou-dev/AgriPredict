package com.example.agripredict.ui.screens.diagnostic

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agripredict.R

/**
 * Écran de diagnostic IA complet et professionnel.
 *
 * Flux utilisateur :
 * 1. Choisir une source : 📷 Caméra OU 🖼️ Galerie
 * 2. L'aperçu s'affiche → appuyer sur "Analyser"
 * 3. Le résultat IA s'affiche (plante, maladie, confiance)
 * 4. Sauvegarder le diagnostic dans la base locale
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    viewModel: DiagnosticViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // === Permission caméra ===
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    // === Lancement de la caméra ===
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.onPhotoCaptured(it) }
    }

    // === Sélection depuis la galerie (Photo Picker moderne Android 13+) ===
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImagePickedFromGallery(it) }
    }

    // Demander la permission au premier lancement
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diagnostic_title)) },
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
        // Contenu scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Afficher selon l'état
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "diagnostic_state"
            ) { state ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (state) {
                        is DiagnosticUiState.Idle -> IdleContent(
                            onTakePhoto = { cameraLauncher.launch(null) },
                            onPickFromGallery = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        is DiagnosticUiState.ImageCaptured -> ImageCapturedContent(
                            bitmap = state.bitmap,
                            onAnalyze = { viewModel.analyzeImage() },
                            onRetakeCamera = { viewModel.reset(); cameraLauncher.launch(null) },
                            onPickFromGallery = {
                                viewModel.reset()
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        is DiagnosticUiState.Analyzing -> AnalyzingContent()
                        is DiagnosticUiState.ResultReady -> ResultContent(
                            bitmap = state.bitmap,
                            plantName = state.plantName,
                            diseaseName = state.diseaseName,
                            isHealthy = state.isHealthy,
                            confidence = state.confidence,
                            isDemo = state.isDemo,
                            onSave = { viewModel.saveDiagnostic() },
                            onNewDiagnostic = { viewModel.reset() }
                        )
                        is DiagnosticUiState.Saved -> SavedContent(
                            onNewDiagnostic = { viewModel.reset() }
                        )
                        is DiagnosticUiState.Error -> ErrorContent(
                            message = state.message,
                            onRetry = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPOSANTS POUR CHAQUE ÉTAT
// ==========================================

/**
 * État initial — Choix entre caméra et galerie.
 */
@Composable
private fun IdleContent(
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    Spacer(modifier = Modifier.height(24.dp))

    // Grande icône centrale avec cercle décoratif
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFlorist,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.diagnostic_no_image),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.diagnostic_instruction),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(36.dp))

    // === Deux boutons côte à côte : Caméra | Galerie ===
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bouton Caméra
        SourceButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CameraAlt,
            label = stringResource(R.string.diagnostic_take_photo),
            isPrimary = true,
            onClick = onTakePhoto
        )

        // Bouton Galerie
        SourceButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.PhotoLibrary,
            label = stringResource(R.string.diagnostic_pick_gallery),
            isPrimary = false,
            onClick = onPickFromGallery
        )
    }
}

/**
 * Bouton source d'image (caméra ou galerie).
 * Design : grande carte verticale cliquable.
 */
@Composable
private fun SourceButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isPrimary)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.secondaryContainer

    val contentColor = if (isPrimary)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Card(
        onClick = onClick,
        modifier = modifier
            .height(130.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = contentColor,
                maxLines = 2
            )
        }
    }
}

/**
 * Image capturée — Aperçu + boutons analyser / changer.
 */
@Composable
private fun ImageCapturedContent(
    bitmap: Bitmap,
    onAnalyze: () -> Unit,
    onRetakeCamera: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    Text(
        text = stringResource(R.string.diagnostic_image_ready),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Aperçu de l'image avec bordure et ombre
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.diagnostic_captured_image),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Bouton Analyser — grand et proéminent
    Button(
        onClick = onAnalyze,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Biotech,
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.diagnostic_analyze),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Boutons secondaires : Reprendre / Galerie
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onRetakeCamera,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.diagnostic_retake), style = MaterialTheme.typography.labelLarge)
        }
        OutlinedButton(
            onClick = onPickFromGallery,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.diagnostic_pick_gallery), style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Analyse en cours — Animation pulsante.
 */
@Composable
private fun AnalyzingContent() {
    Spacer(modifier = Modifier.height(60.dp))

    // Animation pulsante autour de l'icône
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size((120 * scale).dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            strokeWidth = 5.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Text(
        text = stringResource(R.string.diagnostic_analyzing),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.diagnostic_analyzing_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Résultat de l'analyse — Beau design avec carte de résultat.
 */
@Composable
private fun ResultContent(
    bitmap: Bitmap,
    plantName: String,
    diseaseName: String,
    isHealthy: Boolean,
    confidence: Float,
    isDemo: Boolean,
    onSave: () -> Unit,
    onNewDiagnostic: () -> Unit
) {
    // Badge mode démo
    if (isDemo) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Info, null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.diagnostic_demo_mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }

    // Image analysée avec overlay en dégradé
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Badge plante en haut à gauche
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
                    plantName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    // === Carte résultat principale ===
    val statusColor = if (isHealthy) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error

    val statusIcon = if (isHealthy) Icons.Filled.CheckCircle
    else Icons.Filled.Warning

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = statusColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icône statut
            Icon(
                statusIcon, null,
                modifier = Modifier.size(40.dp),
                tint = statusColor
            )

            Spacer(Modifier.height(10.dp))

            // Statut : Saine ou Malade
            Text(
                text = if (isHealthy) stringResource(R.string.diagnostic_result_healthy)
                else stringResource(R.string.diagnostic_result_disease),
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )

            if (!isHealthy && diseaseName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = diseaseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = statusColor.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            // Barre de confiance
            Text(
                text = stringResource(R.string.diagnostic_result_confidence),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            // Pourcentage en grand
            Text(
                text = "${"%.0f".format(confidence * 100)}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    confidence > 0.8f -> MaterialTheme.colorScheme.primary
                    confidence > 0.5f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )

            Spacer(Modifier.height(8.dp))

            // Barre de progression
            LinearProgressIndicator(
                progress = { confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    confidence > 0.8f -> MaterialTheme.colorScheme.primary
                    confidence > 0.5f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                },
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        }
    }

    Spacer(Modifier.height(24.dp))

    // Bouton Sauvegarder
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Filled.Save, null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
    }

    Spacer(Modifier.height(10.dp))

    // Bouton Nouveau diagnostic
    OutlinedButton(
        onClick = onNewDiagnostic,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.diagnostic_new))
    }
}

/**
 * Diagnostic sauvegardé avec succès — Animation de confirmation.
 */
@Composable
private fun SavedContent(onNewDiagnostic: () -> Unit) {
    Spacer(Modifier.height(48.dp))

    // Cercle vert de succès
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.CheckCircle, null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.diagnostic_save_success),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.diagnostic_save_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(36.dp))

    Button(
        onClick = onNewDiagnostic,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Filled.AddAPhoto, null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.diagnostic_new), style = MaterialTheme.typography.titleMedium)
    }
}

/**
 * Erreur survenue — Message + bouton réessayer.
 */
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Spacer(Modifier.height(48.dp))

    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Warning, null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error
        )
    }

    Spacer(Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.error_generic),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(32.dp))

    Button(
        onClick = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.retry))
    }
}
