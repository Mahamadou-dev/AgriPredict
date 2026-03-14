package com.example.agripredict.ui.screens.parcelle

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agripredict.R

/**
 * Écran d'ajout d'une parcelle agricole.
 *
 * Formulaire simple :
 * - Nom de la parcelle (obligatoire)
 * - Commune
 * - Village
 * - Ville
 *
 * Affiché après l'inscription ou depuis l'écran de gestion des parcelles.
 * Le paramètre showSkipButton permet d'afficher "Plus tard" après l'inscription.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParcelleScreen(
    viewModel: ParcelleViewModel,
    onNavigateBack: () -> Unit,
    onParcelleSaved: () -> Unit,
    showSkipButton: Boolean = false,
    onSkip: () -> Unit = {},
    parcelleId: String? = null
) {
    var nomParcelle by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var ville by remember { mutableStateOf("") }
    var nomError by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var initializedForEdit by remember(parcelleId) { mutableStateOf(false) }

    val editingParcelle by viewModel.editingParcelle.collectAsState()
    val isEditMode = !parcelleId.isNullOrBlank()

    LaunchedEffect(parcelleId) {
        if (isEditMode) {
            parcelleId?.let { viewModel.loadParcelleForEdit(it) }
        } else {
            viewModel.clearEditingParcelle()
        }
    }

    LaunchedEffect(editingParcelle, isEditMode, initializedForEdit) {
        if (isEditMode && !initializedForEdit) {
            editingParcelle?.let { parcelle ->
                nomParcelle = parcelle.nomParcelle
                commune = parcelle.commune
                village = parcelle.village
                ville = parcelle.ville
                initializedForEdit = true
            }
        }
    }

    // Naviguer si sauvegardé
    LaunchedEffect(saved) {
        if (saved) onParcelleSaved()
    }

    Scaffold(
        topBar = {
            if (!showSkipButton) {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditMode) stringResource(R.string.parcelle_title)
                            else stringResource(R.string.parcelle_add)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // === Header vert ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2E7D32), Color(0xFF43A047))
                        )
                    )
                    .padding(top = if (showSkipButton) 48.dp else 24.dp, bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Filled.Terrain,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isEditMode) stringResource(R.string.parcelle_title)
                        else stringResource(R.string.parcelle_add_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.parcelle_add_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === Formulaire ===
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Nom de la parcelle (obligatoire)
                    OutlinedTextField(
                        value = nomParcelle,
                        onValueChange = { nomParcelle = it; nomError = false },
                        label = { Text(stringResource(R.string.parcelle_name) + " *") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        isError = nomError,
                        supportingText = if (nomError) {{ Text(stringResource(R.string.field_required)) }} else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Commune
                    OutlinedTextField(
                        value = commune,
                        onValueChange = { commune = it },
                        label = { Text(stringResource(R.string.parcelle_commune)) },
                        leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Village
                    OutlinedTextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(stringResource(R.string.parcelle_village)) },
                        leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ville
                    OutlinedTextField(
                        value = ville,
                        onValueChange = { ville = it },
                        label = { Text(stringResource(R.string.parcelle_ville)) },
                        leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === Bouton Enregistrer ===
            Button(
                onClick = {
                    nomError = nomParcelle.isBlank()
                    if (!nomError) {
                        if (isEditMode) {
                            val existing = editingParcelle
                            if (existing != null) {
                                viewModel.updateParcelle(
                                    existing.copy(
                                        nomParcelle = nomParcelle.trim(),
                                        commune = commune.trim(),
                                        village = village.trim(),
                                        ville = ville.trim()
                                    )
                                )
                                saved = true
                            }
                        } else {
                            viewModel.addParcelle(
                                nomParcelle = nomParcelle.trim(),
                                commune = commune.trim(),
                                village = village.trim(),
                                ville = ville.trim()
                            )
                            saved = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) stringResource(R.string.save)
                    else stringResource(R.string.parcelle_save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // === Bouton "Plus tard" (seulement après inscription) ===
            if (showSkipButton && !isEditMode) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.parcelle_skip),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
