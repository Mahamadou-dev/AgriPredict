package com.example.agripredict.ui.screens.alerts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.agripredict.R
import com.example.agripredict.data.local.entity.AlerteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran des alertes agricoles.
 * Affiche les alertes avec FilterChips cliquables pour filtrer par gravité,
 * et un menu de tri (date, gravité, zone).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val gravityFilter by viewModel.gravityFilter.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_alerts)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Bouton de tri
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.alerts_sort_by)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.alerts_sort_date)) },
                                onClick = {
                                    viewModel.onSortOptionChanged(AlertSortOption.DATE)
                                    showSortMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                                trailingIcon = {
                                    if (sortOption == AlertSortOption.DATE)
                                        Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.alerts_sort_gravity)) },
                                onClick = {
                                    viewModel.onSortOptionChanged(AlertSortOption.GRAVITY)
                                    showSortMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.Warning, null) },
                                trailingIcon = {
                                    if (sortOption == AlertSortOption.GRAVITY)
                                        Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.alerts_sort_zone)) },
                                onClick = {
                                    viewModel.onSortOptionChanged(AlertSortOption.ZONE)
                                    showSortMenu = false
                                },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                                trailingIcon = {
                                    if (sortOption == AlertSortOption.ZONE)
                                        Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // === FilterChips par gravité (cliquables) ===
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = gravityFilter == AlertGravityFilter.ALL,
                        onClick = { viewModel.onGravityFilterChanged(AlertGravityFilter.ALL) },
                        label = { Text(stringResource(R.string.alerts_filter_all)) },
                        leadingIcon = if (gravityFilter == AlertGravityFilter.ALL) {
                            { Icon(Icons.Filled.Done, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = gravityFilter == AlertGravityFilter.HIGH,
                        onClick = { viewModel.onGravityFilterChanged(AlertGravityFilter.HIGH) },
                        label = { Text(stringResource(R.string.alerts_high)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Warning, null,
                                modifier = Modifier.size(16.dp),
                                tint = if (gravityFilter == AlertGravityFilter.HIGH)
                                    MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFFD32F2F)
                            )
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = gravityFilter == AlertGravityFilter.MEDIUM,
                        onClick = { viewModel.onGravityFilterChanged(AlertGravityFilter.MEDIUM) },
                        label = { Text(stringResource(R.string.alerts_medium)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Info, null,
                                modifier = Modifier.size(16.dp),
                                tint = if (gravityFilter == AlertGravityFilter.MEDIUM)
                                    MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFFFF9800)
                            )
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = gravityFilter == AlertGravityFilter.LOW,
                        onClick = { viewModel.onGravityFilterChanged(AlertGravityFilter.LOW) },
                        label = { Text(stringResource(R.string.alerts_low)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.CheckCircle, null,
                                modifier = Modifier.size(16.dp),
                                tint = if (gravityFilter == AlertGravityFilter.LOW)
                                    MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF4CAF50)
                            )
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            when (val state = uiState) {
                is AlertsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is AlertsUiState.Empty -> {
                    AlertsEmptyState()
                }

                is AlertsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is AlertsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.alertes,
                            key = { it.id }
                        ) { alerte ->
                            AlerteCard(alerte = alerte)
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlerteCard(alerte: AlerteEntity) {
    val gravityColor = when {
        alerte.gravite >= 0.7f -> Color(0xFFD32F2F)
        alerte.gravite >= 0.4f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    val gravityIcon = when {
        alerte.gravite >= 0.7f -> Icons.Filled.Warning
        alerte.gravite >= 0.4f -> Icons.Filled.Info
        else -> Icons.Filled.CheckCircle
    }
    val dateStr = remember(alerte.dateEmission) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(alerte.dateEmission))
    }
    val expirationStr = remember(alerte.dateExpiration) {
        alerte.dateExpiration?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête : gravité + zone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = gravityColor.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = gravityIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = gravityColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (alerte.zone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = alerte.zone,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Badge gravité
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = gravityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${(alerte.gravite * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = gravityColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message
            Text(
                text = alerte.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Expiration
            if (expirationStr != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stringResource(R.string.alerts_expires)} $expirationStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = Color(0xFFE8F5E9)
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsNone,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = Color(0xFF4CAF50)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.alerts_empty),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.alerts_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

