package com.example.agripredict.ui.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.data.local.dao.ParcelleDao
import com.example.agripredict.data.local.entity.ParcelleEntity
import com.example.agripredict.data.preferences.SessionPreferences
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.model.Maladie
import com.example.agripredict.domain.repository.DiagnosticRepository
import com.example.agripredict.domain.repository.MaladieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Historique des diagnostics.
 *
 * Responsabilités :
 * - Charger les diagnostics de l'utilisateur connecté depuis Room
 * - Filtrer les diagnostics par recherche
 * - Supprimer un diagnostic
 * - Fournir le détail d'un diagnostic + maladie correspondante
 */
class HistoryViewModel(
    private val repository: DiagnosticRepository,
    private val sessionPreferences: SessionPreferences,
    private val maladieRepository: MaladieRepository,
    private val parcelleDao: ParcelleDao
) : ViewModel() {

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    /** Texte de recherche saisi par l'utilisateur */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** État de l'écran historique */
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    /** Diagnostic sélectionné pour le détail */
    private val _selectedDiagnostic = MutableStateFlow<DiagnosticResult?>(null)
    val selectedDiagnostic: StateFlow<DiagnosticResult?> = _selectedDiagnostic.asStateFlow()

    /** Maladie correspondante au diagnostic sélectionné */
    private val _selectedMaladie = MutableStateFlow<Maladie?>(null)
    val selectedMaladie: StateFlow<Maladie?> = _selectedMaladie.asStateFlow()

    /** Parcelle liée au diagnostic sélectionné */
    private val _selectedParcelle = MutableStateFlow<ParcelleEntity?>(null)
    val selectedParcelle: StateFlow<ParcelleEntity?> = _selectedParcelle.asStateFlow()

    init {
        loadDiagnostics()
    }

    /**
     * Charge les diagnostics de l'utilisateur connecté.
     * Combine le Flow de Room avec le filtre de recherche.
     */
    private fun loadDiagnostics() {
        viewModelScope.launch {
            try {
                val userId = sessionPreferences.getUserId()
                if (userId == null) {
                    _uiState.value = HistoryUiState.Empty
                    return@launch
                }

                combine(
                    repository.observeDiagnosticsByUser(userId),
                    _searchQuery
                ) { diagnostics, query ->
                    if (query.isBlank()) {
                        diagnostics
                    } else {
                        diagnostics.filter { diagnostic ->
                            diagnostic.label.contains(query, ignoreCase = true)
                        }
                    }
                }.collect { filteredList ->
                    _uiState.value = if (filteredList.isEmpty()) {
                        HistoryUiState.Empty
                    } else {
                        HistoryUiState.Success(filteredList)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur chargement historique : ${e.message}", e)
                _uiState.value = HistoryUiState.Error(e.message ?: "Erreur de chargement")
            }
        }
    }

    /** Met à jour le texte de recherche */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Sélectionne un diagnostic pour afficher le détail */
    fun selectDiagnostic(diagnostic: DiagnosticResult) {
        _selectedDiagnostic.value = diagnostic
    }

    /** Charge un diagnostic par son ID + la maladie correspondante */
    fun loadDiagnosticById(diagnosticId: String) {
        viewModelScope.launch {
            try {
                val diagnostic = repository.getDiagnosticById(diagnosticId)
                _selectedDiagnostic.value = diagnostic
                _selectedParcelle.value = null

                // Rechercher la maladie correspondante dans la BDD
                if (diagnostic != null) {
                    try {
                        val maladie = maladieRepository.findByLabel(diagnostic.label)
                        _selectedMaladie.value = maladie
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erreur recherche maladie : ${e.message}")
                        _selectedMaladie.value = null
                    }

                    // Charger les infos minimales de la parcelle du diagnostic
                    val parcelleId = diagnostic.parcelleId
                    if (!parcelleId.isNullOrBlank()) {
                        _selectedParcelle.value = parcelleDao.getById(parcelleId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur chargement diagnostic $diagnosticId : ${e.message}", e)
            }
        }
    }

    /** Supprime un diagnostic */
    fun deleteDiagnostic(diagnosticId: String) {
        viewModelScope.launch {
            try {
                repository.deleteDiagnostic(diagnosticId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur suppression diagnostic $diagnosticId : ${e.message}", e)
            }
        }
    }

    // === Factory pour l'injection de dépendances ===

    class Factory(
        private val repository: DiagnosticRepository,
        private val sessionPreferences: SessionPreferences,
        private val maladieRepository: MaladieRepository,
        private val parcelleDao: ParcelleDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository, sessionPreferences, maladieRepository, parcelleDao) as T
        }
    }
}

/**
 * États possibles de l'écran Historique.
 */
sealed class HistoryUiState {
    /** Chargement en cours */
    data object Loading : HistoryUiState()

    /** Aucun diagnostic trouvé */
    data object Empty : HistoryUiState()

    /** Liste de diagnostics disponible */
    data class Success(val diagnostics: List<DiagnosticResult>) : HistoryUiState()

    /** Erreur de chargement */
    data class Error(val message: String) : HistoryUiState()
}

