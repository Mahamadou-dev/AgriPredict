package com.example.agripredict.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.agripredict.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Écran de profil de l'agriculteur.
 *
 * Affiche les informations de l'utilisateur connecté.
 * Permet de :
 * - Modifier les informations personnelles (nom, téléphone, commune, village)
 * - Changer le mot de passe
 * - Se déconnecter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val profileUpdateState by viewModel.profileUpdateState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // Charger le profil au premier affichage
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Snackbar pour les messages de succès/erreur
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.profile_save_success)
    val passwordChangedMessage = stringResource(R.string.profile_password_changed)
    val wrongPasswordMessage = stringResource(R.string.profile_wrong_current_password)
    val phoneExistsMessage = stringResource(R.string.auth_phone_exists)

    LaunchedEffect(profileUpdateState) {
        when (profileUpdateState) {
            is ProfileUpdateState.Success -> {
                snackbarHostState.showSnackbar(successMessage)
                viewModel.resetProfileUpdateState()
            }
            is ProfileUpdateState.PasswordChanged -> {
                snackbarHostState.showSnackbar(passwordChangedMessage)
                viewModel.resetProfileUpdateState()
            }
            is ProfileUpdateState.Error -> {
                val errorMsg = when ((profileUpdateState as ProfileUpdateState.Error).errorCode) {
                    "wrong_current_password" -> wrongPasswordMessage
                    "phone_already_exists" -> phoneExistsMessage
                    else -> ""
                }
                if (errorMsg.isNotEmpty()) snackbarHostState.showSnackbar(errorMsg)
                viewModel.resetProfileUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // === Avatar ===
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === Nom de l'utilisateur ===
            Text(
                text = userProfile?.nom ?: "...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = userProfile?.role ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // === Informations détaillées ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Téléphone
                    ProfileInfoRow(
                        icon = Icons.Filled.Phone,
                        label = stringResource(R.string.auth_phone),
                        value = userProfile?.telephone ?: ""
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Commune
                    ProfileInfoRow(
                        icon = Icons.Filled.LocationCity,
                        label = stringResource(R.string.auth_commune),
                        value = userProfile?.commune ?: ""
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Village
                    ProfileInfoRow(
                        icon = Icons.Filled.Place,
                        label = stringResource(R.string.auth_village),
                        value = userProfile?.village ?: ""
                    )

                    // Dernier login
                    userProfile?.lastLogin?.let { timestamp ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        ProfileInfoRow(
                            icon = Icons.Filled.Schedule,
                            label = stringResource(R.string.profile_last_login),
                            value = formatDate(timestamp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === Bouton Modifier le profil ===
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.profile_edit),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Bouton Changer le mot de passe ===
            OutlinedButton(
                onClick = { showPasswordDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.profile_change_password),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Bouton Déconnexion ===
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.auth_logout),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // === Dialogue de confirmation de déconnexion ===
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.auth_logout)) },
            text = { Text(stringResource(R.string.auth_logout_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    }
                ) {
                    Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // === Dialogue de modification du profil ===
    if (showEditDialog) {
        EditProfileDialog(
            currentNom = userProfile?.nom ?: "",
            currentTelephone = userProfile?.telephone ?: "",
            currentCommune = userProfile?.commune ?: "",
            currentVillage = userProfile?.village ?: "",
            isLoading = profileUpdateState is ProfileUpdateState.Loading,
            onDismiss = { showEditDialog = false },
            onSave = { nom, telephone, commune, village ->
                viewModel.updateProfile(nom, telephone, commune, village)
                showEditDialog = false
            }
        )
    }

    // === Dialogue de changement de mot de passe ===
    if (showPasswordDialog) {
        ChangePasswordDialog(
            isLoading = profileUpdateState is ProfileUpdateState.Loading,
            onDismiss = { showPasswordDialog = false },
            onChangePassword = { currentPwd, newPwd ->
                viewModel.changePassword(currentPwd, newPwd)
                showPasswordDialog = false
            }
        )
    }
}

// ==========================================
// Dialogue : Modifier le profil
// ==========================================

@Composable
private fun EditProfileDialog(
    currentNom: String,
    currentTelephone: String,
    currentCommune: String,
    currentVillage: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (nom: String, telephone: String, commune: String, village: String) -> Unit
) {
    var nom by remember { mutableStateOf(currentNom) }
    var telephone by remember { mutableStateOf(currentTelephone) }
    var commune by remember { mutableStateOf(currentCommune) }
    var village by remember { mutableStateOf(currentVillage) }
    var nomError by remember { mutableStateOf(false) }
    var telephoneError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profile_edit_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it; nomError = false },
                    label = { Text(stringResource(R.string.auth_name)) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    isError = nomError,
                    supportingText = {
                        if (nomError) Text(stringResource(R.string.auth_field_required))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it; telephoneError = false },
                    label = { Text(stringResource(R.string.auth_phone)) },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    isError = telephoneError,
                    supportingText = {
                        if (telephoneError) Text(stringResource(R.string.auth_phone_error))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = commune,
                    onValueChange = { commune = it },
                    label = { Text(stringResource(R.string.auth_commune)) },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text(stringResource(R.string.auth_village)) },
                    leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nomError = nom.isBlank()
                    telephoneError = telephone.length < 8
                    if (!nomError && !telephoneError) {
                        onSave(nom.trim(), telephone.trim(), commune.trim(), village.trim())
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ==========================================
// Dialogue : Changer le mot de passe
// ==========================================

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPwdVisible by remember { mutableStateOf(false) }
    var newPwdVisible by remember { mutableStateOf(false) }
    var currentPwdError by remember { mutableStateOf(false) }
    var newPwdError by remember { mutableStateOf(false) }
    var confirmPwdError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profile_change_password),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Mot de passe actuel
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; currentPwdError = false },
                    label = { Text(stringResource(R.string.profile_current_password)) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { currentPwdVisible = !currentPwdVisible }) {
                            Icon(
                                if (currentPwdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    isError = currentPwdError,
                    supportingText = {
                        if (currentPwdError) Text(stringResource(R.string.auth_password_error))
                    },
                    visualTransformation = if (currentPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Nouveau mot de passe
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; newPwdError = false },
                    label = { Text(stringResource(R.string.profile_new_password)) },
                    leadingIcon = { Icon(Icons.Filled.LockOpen, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { newPwdVisible = !newPwdVisible }) {
                            Icon(
                                if (newPwdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    isError = newPwdError,
                    supportingText = {
                        if (newPwdError) Text(stringResource(R.string.profile_password_too_short))
                    },
                    visualTransformation = if (newPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Confirmer mot de passe
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPwdError = false },
                    label = { Text(stringResource(R.string.profile_confirm_password)) },
                    leadingIcon = { Icon(Icons.Filled.LockOpen, contentDescription = null) },
                    isError = confirmPwdError,
                    supportingText = {
                        if (confirmPwdError) Text(stringResource(R.string.profile_password_mismatch))
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    currentPwdError = currentPassword.isEmpty()
                    newPwdError = newPassword.length < 4
                    confirmPwdError = newPassword != confirmPassword
                    if (!currentPwdError && !newPwdError && !confirmPwdError) {
                        onChangePassword(currentPassword, newPassword)
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ==========================================
// Composant réutilisable : ligne d'info profil
// ==========================================

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Formate un timestamp en date lisible selon la locale.
 */
private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
