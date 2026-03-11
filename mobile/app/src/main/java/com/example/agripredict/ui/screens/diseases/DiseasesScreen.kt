package com.example.agripredict.ui.screens.diseases

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.agripredict.R
import com.example.agripredict.domain.model.Maladie
import com.example.agripredict.ui.components.TraitementCard

/**
 * Écran de la base de connaissances sur les maladies des plantes.
 * Espace d'apprentissage interactif et catégorisé avec FilterChips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseasesScreen(
    viewModel: DiseasesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPlant by viewModel.selectedPlant.collectAsState()
    val availablePlants by viewModel.availablePlants.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_diseases)) },
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
                placeholder = { Text(stringResource(R.string.diseases_search)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
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

            // === FilterChips par catégorie de plante ===
            if (availablePlants.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip "Toutes"
                    item {
                        FilterChip(
                            selected = selectedPlant == null,
                            onClick = { viewModel.onPlantFilterChanged(null) },
                            label = { Text(stringResource(R.string.diseases_all_plants)) },
                            leadingIcon = if (selectedPlant == null) {
                                { Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    // Un chip par plante
                    items(availablePlants) { plant ->
                        val (icon, color) = remember(plant) {
                            when (plant) {
                                "Manioc" -> Icons.Filled.Grass to Color(0xFF2E7D32)
                                "Maïs" -> Icons.Filled.Grain to Color(0xFFF9A825)
                                "Tomate" -> Icons.Filled.Spa to Color(0xFFD32F2F)
                                "Poivron" -> Icons.Filled.Eco to Color(0xFFE65100)
                                "Pomme de terre" -> Icons.Filled.Yard to Color(0xFF795548)
                                else -> Icons.Filled.Spa to Color(0xFF2E7D32)
                            }
                        }
                        FilterChip(
                            selected = selectedPlant == plant,
                            onClick = {
                                viewModel.onPlantFilterChanged(if (selectedPlant == plant) null else plant)
                            },
                            label = { Text(plant) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedPlant == plant) MaterialTheme.colorScheme.onSecondaryContainer else color
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val state = uiState) {
                is DiseasesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is DiseasesUiState.Empty -> {
                    DiseasesEmptyState()
                }

                is DiseasesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is DiseasesUiState.Success -> {
                    // Grouper par plante
                    val grouped: Map<String, List<Maladie>> = remember(state.maladies) {
                        state.maladies.groupBy<Maladie, String> { maladie: Maladie ->
                            getPlantCategory(maladie.nomCommun)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Compteur total
                        item {
                            Text(
                                text = String.format(stringResource(R.string.diseases_total), state.maladies.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        grouped.forEach { (plantName: String, maladiesList: List<Maladie>) ->
                            item(key = "header_$plantName") {
                                PlantGroupHeader(plantName = plantName, count = maladiesList.size)
                            }

                            items(
                                items = maladiesList,
                                key = { item: Maladie -> "maladie_${item.id}" }
                            ) { maladie: Maladie ->
                                MaladieExpandableCard(maladie = maladie)
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlantGroupHeader(plantName: String, count: Int) {
    val (icon, color) = remember(plantName) {
        when {
            plantName.contains("Manioc", ignoreCase = true) -> Icons.Filled.Grass to Color(0xFF2E7D32)
            plantName.contains("Maïs", ignoreCase = true) -> Icons.Filled.Grain to Color(0xFFF9A825)
            plantName.contains("Tomate", ignoreCase = true) -> Icons.Filled.Spa to Color(0xFFD32F2F)
            plantName.contains("Poivron", ignoreCase = true) -> Icons.Filled.Eco to Color(0xFFE65100)
            plantName.contains("Pomme", ignoreCase = true) -> Icons.Filled.Yard to Color(0xFF795548)
            else -> Icons.Filled.Spa to Color(0xFF2E7D32)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = plantName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MaladieExpandableCard(maladie: Maladie) {
    var expanded by remember { mutableStateOf(false) }
    val isHealthy = maladie.nomCommun.contains("sain", ignoreCase = true)

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(350)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHealthy)
                Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336)
                ) {}
                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = maladie.nomCommun,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (maladie.nomScientifique.isNotEmpty()) {
                        Text(
                            text = maladie.nomScientifique,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Contenu dépliable
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Description avec section titre
                    if (maladie.description.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.diseases_description_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = maladie.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Traitements
                    if (maladie.traitements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.diseases_treatments),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        maladie.traitements.forEach { traitement ->
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
    }
}

@Composable
private fun DiseasesEmptyState() {
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
                imageVector = Icons.Filled.Spa,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.diseases_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.diseases_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Catégorise une maladie par plante à partir du nom commun. */
private fun getPlantCategory(nomCommun: String): String {
    return when {
        nomCommun.contains("manioc", ignoreCase = true) || nomCommun.contains("cassava", ignoreCase = true) -> "Manioc"
        nomCommun.contains("maïs", ignoreCase = true) || nomCommun.contains("corn", ignoreCase = true) -> "Maïs"
        nomCommun.contains("tomate", ignoreCase = true) || nomCommun.contains("tomato", ignoreCase = true) -> "Tomate"
        nomCommun.contains("poivron", ignoreCase = true) || nomCommun.contains("pepper", ignoreCase = true) -> "Poivron"
        nomCommun.contains("pomme de terre", ignoreCase = true) || nomCommun.contains("potato", ignoreCase = true) -> "Pomme de terre"
        else -> "Autres"
    }
}
