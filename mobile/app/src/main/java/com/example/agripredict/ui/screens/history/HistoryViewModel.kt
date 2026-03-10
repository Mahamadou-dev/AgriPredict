package com.example.agripredict.ui.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.data.preferences.SessionPreferences
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.repository.DiagnosticRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Historique des diagnostics.
 *
 * Responsabilités :
 * - Charger les diagnostics de l'utilisateur connecté depuis Room
 * - Filtrer les diagnostics par recherche
 * - Supprimer un diagnostic
 * - Fournir le détail d'un diagnostic
 */
class HistoryViewModel(
    private val repository: DiagnosticRepository,
    private val sessionPreferences: SessionPreferences
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

    init {
        loadDiagnostics()
    }

    /**
     * Charge les diagnostics de l'utilisateur connecté.
     * Combine le Flow de Room avec le filtre de recherche.
     * Protégé par try-catch pour éviter un crash si la DB a un problème.
     */
    private fun loadDiagnostics() {
        viewModelScope.launch {
            try {
                val userId = sessionPreferences.getUserId()
                if (userId == null) {
                    _uiState.value = HistoryUiState.Empty
                    return@launch
                }

                // Combiner les diagnostics avec la recherche
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

    /** Charge un diagnostic par son ID (pour la navigation par route) */
    fun loadDiagnosticById(diagnosticId: String) {
        viewModelScope.launch {
            try {
                val diagnostic = repository.getDiagnosticById(diagnosticId)
                _selectedDiagnostic.value = diagnostic
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
                // La liste se met à jour automatiquement via Flow
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur suppression diagnostic $diagnosticId : ${e.message}", e)
            }
        }
    }

    // === Factory pour l'injection de dépendances ===

    class Factory(
        private val repository: DiagnosticRepository,
        private val sessionPreferences: SessionPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository, sessionPreferences) as T
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

