package com.example.agripredict.ui.screens.expert

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agripredict.R

/**
 * Écran pour contacter un expert agricole.
 * Annuaire avec appel direct et SMS + formulaire email.
 * Conçu pour des agriculteurs non familiers avec le digital :
 * - Gros boutons, icônes explicites, couleurs fortes
 * - Appel et SMS fonctionnent sans connexion internet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var subjectText by remember { mutableStateOf("") }
    var showEmailForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_expert)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === En-tête ===
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color(0xFFF3E5F5)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SupportAgent,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            tint = Color(0xFF6A1B9A)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.expert_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.expert_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // === Section Annuaire ===
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.ContactPhone,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.expert_directory_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.expert_directory_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // === Cartes experts ===
            itemsIndexed(expertContacts) { index, expert ->
                ExpertContactCard(
                    expert = expert,
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${expert.phone}")
                        }
                        context.startActivity(intent)
                    },
                    onSms = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${expert.phone}")
                            putExtra("sms_body", "Bonjour ${expert.name}, je vous contacte via AgriPredict pour une consultation agricole.")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // === Séparateur + Option Email ===
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Bouton pour afficher/masquer le formulaire email
                OutlinedButton(
                    onClick = { showEmailForm = !showEmailForm },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        if (showEmailForm) Icons.Filled.ExpandLess else Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.expert_or_send_message),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // === Formulaire Email (caché par défaut) ===
            item {
                AnimatedVisibility(
                    visible = showEmailForm,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.expert_form_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = subjectText,
                                onValueChange = { subjectText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.expert_subject)) },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                label = { Text(stringResource(R.string.expert_message)) },
                                leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:expert@agripredict.ne")
                                        putExtra(Intent.EXTRA_SUBJECT, subjectText.ifEmpty { "AgriPredict - Consultation" })
                                        putExtra(Intent.EXTRA_TEXT, messageText)
                                    }
                                    context.startActivity(intent)
                                    subjectText = ""
                                    messageText = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = messageText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.expert_send))
                            }
                        }
                    }
                }
            }

            // === Astuce ===
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8E1)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFF9A825),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.expert_tip),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Carte de contact expert avec gros boutons Appeler et SMS.
 * Design adapté aux agriculteurs : boutons très grands, icônes explicites.
 */
@Composable
private fun ExpertContactCard(
    expert: ExpertContact,
    onCall: () -> Unit,
    onSms: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar avec initiales
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = expert.initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expert.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = expert.specialty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = expert.zone,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Gros boutons Appeler et SMS côte à côte ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Bouton APPELER — Vert, grand
                Button(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    )
                ) {
                    Icon(
                        Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.expert_call_action),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Bouton SMS — Bleu, grand
                OutlinedButton(
                    onClick = onSms,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1565C0)
                    )
                ) {
                    Icon(
                        Icons.Filled.Sms,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.expert_sms_action),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
