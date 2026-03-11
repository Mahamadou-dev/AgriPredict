package com.example.agripredict.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Barre de confiance animée réutilisable.
 * Affiche le pourcentage + barre de progression colorée selon le niveau.
 */
@Composable
fun ConfidenceBar(
    confidence: Float,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    label: String = "Confiance"
) {
    val confidencePercent = (confidence * 100).toInt()
    val confColor = when {
        confidence > 0.8f -> Color(0xFF4CAF50)
        confidence > 0.5f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Column(modifier = modifier.animateContentSize()) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$confidencePercent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = confColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = confColor,
            trackColor = confColor.copy(alpha = 0.12f)
        )
    }
}

/**
 * Badge statut sain/malade réutilisable.
 */
@Composable
fun StatusBadge(
    isHealthy: Boolean,
    healthyText: String = "Plante saine",
    diseaseText: String = "Maladie détectée",
    modifier: Modifier = Modifier
) {
    val color = if (isHealthy) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isHealthy) Icons.Filled.CheckCircle else Icons.Filled.Warning
    val text = if (isHealthy) healthyText else diseaseText

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Carte de traitement réutilisable pour diagnostic, historique et maladies.
 */
@Composable
fun TraitementCard(
    titre: String,
    description: String,
    dosage: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Healing,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = titre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (dosage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Science,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dosage,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Composant état vide générique avec icône, titre et sous-titre.
 */
@Composable
fun EmptyStateComponent(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    bgColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = bgColor
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

