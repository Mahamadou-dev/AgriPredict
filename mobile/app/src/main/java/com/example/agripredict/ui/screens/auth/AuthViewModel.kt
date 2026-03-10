package com.example.agripredict.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.data.local.dao.UserDao
import com.example.agripredict.data.local.entity.UserEntity
import com.example.agripredict.data.preferences.SessionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel pour l'authentification (inscription, connexion, profil).
 *
 * Gère l'état global de la session utilisateur.
 * Communique avec UserDao (Room) et SessionPreferences (DataStore).
 */
class AuthViewModel(
    private val userDao: UserDao,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // === État observable par l'UI ===
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // État du profil utilisateur chargé
    private val _userProfile = MutableStateFlow<UserEntity?>(null)
    val userProfile: StateFlow<UserEntity?> = _userProfile.asStateFlow()

    // État pour les opérations sur le profil (édition, mot de passe)
    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState.asStateFlow()

    init {
        // Au démarrage, vérifier si une session existe
        checkSession()
    }

    // ==========================================
    // Vérification de session
    // ==========================================

    /**
     * Vérifie si l'utilisateur est déjà connecté (session DataStore).
     * Protégé par try-catch pour éviter un crash au démarrage
     * si la base de données a un problème (migration, corruption, etc.).
     */
    private fun checkSession() {
        viewModelScope.launch {
            try {
                val isLoggedIn = sessionPreferences.isLoggedIn.first()
                if (isLoggedIn) {
                    val userId = sessionPreferences.userId.first()
                    val user = userDao.getById(userId)
                    if (user != null) {
                        _userProfile.value = user
                        _authState.value = AuthUiState.LoggedIn(user.id, user.nom)
                    } else {
                        // Session invalide → nettoyer
                        Log.w(TAG, "⚠️ Session invalide : utilisateur $userId introuvable en DB")
                        sessionPreferences.clearSession()
                        _authState.value = AuthUiState.LoggedOut
                    }
                } else {
                    _authState.value = AuthUiState.LoggedOut
                }
            } catch (e: Exception) {
                // En cas d'erreur DB/DataStore au démarrage, on redirige vers login
                Log.e(TAG, "❌ Erreur checkSession : ${e.message}", e)
                try {
                    sessionPreferences.clearSession()
                } catch (_: Exception) { /* ignore */ }
                _authState.value = AuthUiState.LoggedOut
            }
        }
    }

    // ==========================================
    // Inscription (avec mot de passe par défaut 123456)
    // ==========================================

    /**
     * Inscrit un nouvel agriculteur.
     * Le mot de passe par défaut est "123456".
     */
    fun register(nom: String, telephone: String, commune: String, village: String, password: String = "123456") {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            try {
                // Vérifier si le téléphone existe déjà
                val existingCount = userDao.countByTelephone(telephone)
                if (existingCount > 0) {
                    _authState.value = AuthUiState.Error("phone_already_exists")
                    return@launch
                }

                // Créer l'utilisateur
                val userId = UUID.randomUUID().toString()
                val user = UserEntity(
                    id = userId,
                    nom = nom,
                    telephone = telephone,
                    password = password,
                    commune = commune,
                    village = village,
                    role = "agriculteur",
                    isActive = true,
                    lastLogin = System.currentTimeMillis()
                )

                // Sauvegarder dans Room
                userDao.insert(user)

                // Sauvegarder la session
                sessionPreferences.saveSession(userId, nom)

                _userProfile.value = user
                _authState.value = AuthUiState.LoggedIn(userId, nom)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur inscription : ${e.message}", e)
                _authState.value = AuthUiState.Error("registration_failed")
            }
        }
    }

    // ==========================================
    // Connexion (téléphone + mot de passe)
    // ==========================================

    /**
     * Connecte un agriculteur par téléphone + mot de passe.
     */
    fun login(telephone: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            try {
                val user = userDao.getByTelephone(telephone)
                if (user == null) {
                    _authState.value = AuthUiState.Error("user_not_found")
                    return@launch
                }

                // Vérifier le mot de passe
                if (user.password != password) {
                    _authState.value = AuthUiState.Error("wrong_password")
                    return@launch
                }

                // Mettre à jour le dernier login
                val updatedUser = user.copy(lastLogin = System.currentTimeMillis())
                userDao.update(updatedUser)

                // Sauvegarder la session
                sessionPreferences.saveSession(user.id, user.nom)

                _userProfile.value = updatedUser
                _authState.value = AuthUiState.LoggedIn(user.id, user.nom)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur connexion : ${e.message}", e)
                _authState.value = AuthUiState.Error("login_failed")
            }
        }
    }

    // ==========================================
    // Déconnexion
    // ==========================================

    /**
     * Déconnecte l'utilisateur et efface la session.
     */
    fun logout() {
        viewModelScope.launch {
            sessionPreferences.clearSession()
            _userProfile.value = null
            _authState.value = AuthUiState.LoggedOut
        }
    }

    // ==========================================
    // Chargement du profil
    // ==========================================

    /**
     * Charge le profil complet de l'utilisateur connecté.
     */
    fun loadProfile() {
        viewModelScope.launch {
            try {
                val userId = sessionPreferences.userId.first()
                if (userId.isNotEmpty()) {
                    _userProfile.value = userDao.getById(userId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur chargement profil : ${e.message}", e)
            }
        }
    }

    // ==========================================
    // Mise à jour du profil
    // ==========================================

    /**
     * Met à jour les informations personnelles de l'utilisateur.
     */
    fun updateProfile(nom: String, telephone: String, commune: String, village: String) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading

            try {
                val currentUser = _userProfile.value
                if (currentUser == null) {
                    _profileUpdateState.value = ProfileUpdateState.Error("no_user")
                    return@launch
                }

                // Si le téléphone a changé, vérifier l'unicité
                if (telephone != currentUser.telephone) {
                    val existingCount = userDao.countByTelephone(telephone)
                    if (existingCount > 0) {
                        _profileUpdateState.value = ProfileUpdateState.Error("phone_already_exists")
                        return@launch
                    }
                }

                val updatedUser = currentUser.copy(
                    nom = nom,
                    telephone = telephone,
                    commune = commune,
                    village = village
                )

                userDao.update(updatedUser)
                sessionPreferences.saveSession(updatedUser.id, updatedUser.nom)

                _userProfile.value = updatedUser
                _authState.value = AuthUiState.LoggedIn(updatedUser.id, updatedUser.nom)
                _profileUpdateState.value = ProfileUpdateState.Success
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur mise à jour profil : ${e.message}", e)
                _profileUpdateState.value = ProfileUpdateState.Error("update_failed")
            }
        }
    }

    // ==========================================
    // Changement de mot de passe
    // ==========================================

    /**
     * Change le mot de passe de l'utilisateur.
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading

            try {
                val user = _userProfile.value
                if (user == null) {
                    _profileUpdateState.value = ProfileUpdateState.Error("no_user")
                    return@launch
                }

                // Vérifier l'ancien mot de passe
                if (user.password != currentPassword) {
                    _profileUpdateState.value = ProfileUpdateState.Error("wrong_current_password")
                    return@launch
                }

                val updatedUser = user.copy(password = newPassword)
                userDao.update(updatedUser)

                _userProfile.value = updatedUser
                _profileUpdateState.value = ProfileUpdateState.PasswordChanged
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur changement mot de passe : ${e.message}", e)
                _profileUpdateState.value = ProfileUpdateState.Error("password_change_failed")
            }
        }
    }

    /**
     * Remet l'état de mise à jour du profil à Idle.
     */
    fun resetProfileUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Idle
    }

    // ==========================================
    // Factory pour injection manuelle
    // ==========================================

    class Factory(
        private val userDao: UserDao,
        private val sessionPreferences: SessionPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(userDao, sessionPreferences) as T
            }
            throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
        }
    }
}

// ==========================================
// États de l'authentification
// ==========================================

sealed class AuthUiState {
    /** Vérification de la session en cours */
    data object Loading : AuthUiState()

    /** Utilisateur non connecté */
    data object LoggedOut : AuthUiState()

    /** Utilisateur connecté */
    data class LoggedIn(val userId: String, val userName: String) : AuthUiState()

    /** Erreur (code d'erreur pour i18n) */
    data class Error(val errorCode: String) : AuthUiState()
}

// ==========================================
// États de mise à jour du profil
// ==========================================

sealed class ProfileUpdateState {
    data object Idle : ProfileUpdateState()
    data object Loading : ProfileUpdateState()
    data object Success : ProfileUpdateState()
    data object PasswordChanged : ProfileUpdateState()
    data class Error(val errorCode: String) : ProfileUpdateState()
}
