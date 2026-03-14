package com.example.agripredict.ui.screens.parcelle

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.data.local.dao.ParcelleDao
import com.example.agripredict.data.local.entity.ParcelleEntity
import com.example.agripredict.data.preferences.SessionPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel pour la gestion des parcelles agricoles.
 *
 * Responsabilités :
 * - Lister les parcelles de l'utilisateur connecté
 * - Ajouter / modifier / supprimer une parcelle
 */
class ParcelleViewModel(
    private val parcelleDao: ParcelleDao,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "ParcelleViewModel"
    }

    /** Liste des parcelles de l'utilisateur */
    private val _parcelles = MutableStateFlow<List<ParcelleEntity>>(emptyList())
    val parcelles: StateFlow<List<ParcelleEntity>> = _parcelles.asStateFlow()

    /** État de l'écran */
    private val _uiState = MutableStateFlow<ParcelleUiState>(ParcelleUiState.Loading)
    val uiState: StateFlow<ParcelleUiState> = _uiState.asStateFlow()

    /** Parcelle actuellement chargée pour édition */
    private val _editingParcelle = MutableStateFlow<ParcelleEntity?>(null)
    val editingParcelle: StateFlow<ParcelleEntity?> = _editingParcelle.asStateFlow()

    init {
        loadParcelles()
    }

    /**
     * Charge les parcelles de l'utilisateur connecté.
     */
    private fun loadParcelles() {
        viewModelScope.launch {
            try {
                val userId = sessionPreferences.userId.first()
                if (userId.isEmpty()) {
                    _uiState.value = ParcelleUiState.Empty
                    return@launch
                }

                parcelleDao.observeByUser(userId).collect { list ->
                    _parcelles.value = list
                    _uiState.value = if (list.isEmpty()) {
                        ParcelleUiState.Empty
                    } else {
                        ParcelleUiState.Success(list)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur chargement parcelles : ${e.message}", e)
                _uiState.value = ParcelleUiState.Error(e.message ?: "Erreur")
            }
        }
    }

    /**
     * Ajoute une nouvelle parcelle pour l'utilisateur connecté.
     */
    fun addParcelle(nomParcelle: String, commune: String, village: String, ville: String) {
        viewModelScope.launch {
            try {
                val userId = sessionPreferences.userId.first()
                if (userId.isEmpty()) return@launch

                val parcelle = ParcelleEntity(
                    id = UUID.randomUUID().toString(),
                    nomParcelle = nomParcelle,
                    commune = commune,
                    village = village,
                    ville = ville,
                    utilisateurId = userId
                )
                parcelleDao.insert(parcelle)
                Log.d(TAG, "✅ Parcelle ajoutée : ${parcelle.nomParcelle}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur ajout parcelle : ${e.message}", e)
            }
        }
    }

    /**
     * Met à jour une parcelle existante.
     */
    fun updateParcelle(parcelle: ParcelleEntity) {
        viewModelScope.launch {
            try {
                parcelleDao.update(parcelle)
                Log.d(TAG, "✅ Parcelle mise à jour : ${parcelle.nomParcelle}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur mise à jour parcelle : ${e.message}", e)
            }
        }
    }

    /**
     * Supprime une parcelle.
     */
    fun deleteParcelle(parcelle: ParcelleEntity) {
        viewModelScope.launch {
            try {
                parcelleDao.delete(parcelle)
                Log.d(TAG, "🗑️ Parcelle supprimée : ${parcelle.nomParcelle}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur suppression parcelle : ${e.message}", e)
            }
        }
    }

    /** Charge une parcelle par ID pour alimenter le formulaire d'édition. */
    fun loadParcelleForEdit(parcelleId: String) {
        viewModelScope.launch {
            try {
                _editingParcelle.value = parcelleDao.getById(parcelleId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur chargement parcelle $parcelleId : ${e.message}", e)
                _editingParcelle.value = null
            }
        }
    }

    /** Efface la parcelle en cours d'édition (retour au mode ajout). */
    fun clearEditingParcelle() {
        _editingParcelle.value = null
    }

    // === Factory ===

    class Factory(
        private val parcelleDao: ParcelleDao,
        private val sessionPreferences: SessionPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ParcelleViewModel::class.java)) {
                return ParcelleViewModel(parcelleDao, sessionPreferences) as T
            }
            throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
        }
    }
}

// ==========================================
// États de l'écran parcelles
// ==========================================

sealed class ParcelleUiState {
    data object Loading : ParcelleUiState()
    data object Empty : ParcelleUiState()
    data class Success(val parcelles: List<ParcelleEntity>) : ParcelleUiState()
    data class Error(val message: String) : ParcelleUiState()
}
