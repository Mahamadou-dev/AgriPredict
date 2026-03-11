package com.example.agripredict.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.agripredict.R

/**
 * Écran "À propos" de l'application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_about)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // === Logo / Icône de l'app ===
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = Color(0xFFE8F5E9)
            ) {
                Icon(
                    imageVector = Icons.Filled.Eco,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    tint = Color(0xFF2E7D32)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // === Projet ===
            AboutSectionCard(
                title = stringResource(R.string.about_project_title),
                icon = Icons.Filled.School,
                iconColor = Color(0xFF1565C0)
            ) {
                AboutInfoRow(
                    label = stringResource(R.string.about_project_type),
                    value = stringResource(R.string.about_project_type_value)
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_university),
                    value = stringResource(R.string.about_university_value)
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_year),
                    value = "2024 – 2025"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Auteur ===
            AboutSectionCard(
                title = stringResource(R.string.about_author_title),
                icon = Icons.Filled.Person,
                iconColor = Color(0xFF6A1B9A)
            ) {
                AboutInfoRow(
                    label = stringResource(R.string.about_author),
                    value = stringResource(R.string.about_author_value)
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_supervisor),
                    value = stringResource(R.string.about_supervisor_value)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Stack technique ===
            AboutSectionCard(
                title = stringResource(R.string.about_tech_title),
                icon = Icons.Filled.Code,
                iconColor = Color(0xFFE65100)
            ) {
                TechChipRow(
                    chips = listOf(
                        "Kotlin", "Jetpack Compose", "Room DB",
                        "TensorFlow Lite", "MobileNetV2", "Material 3"
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Modèle IA ===
            AboutSectionCard(
                title = stringResource(R.string.about_ai_title),
                icon = Icons.Filled.Psychology,
                iconColor = Color(0xFF00838F)
            ) {
                AboutInfoRow(
                    label = stringResource(R.string.about_model),
                    value = "MobileNetV2 (INT8)"
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_classes),
                    value = "24"
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_crops),
                    value = stringResource(R.string.about_crops_value)
                )
                AboutInfoRow(
                    label = stringResource(R.string.about_accuracy),
                    value = "~95%"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === Mention ===
            Text(
                text = stringResource(R.string.about_footer),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TechChipRow(chips: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

