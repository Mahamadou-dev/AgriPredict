package com.example.agripredict.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.data.local.dao.AlerteDao
import com.example.agripredict.data.local.entity.AlerteEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Options de tri pour les alertes.
 */
enum class AlertSortOption {
    DATE, GRAVITY, ZONE
}

/**
 * Options de filtre par gravité.
 */
enum class AlertGravityFilter {
    ALL, HIGH, MEDIUM, LOW
}

/**
 * ViewModel pour l'écran Alertes agricoles.
 * Supporte le tri et le filtrage par gravité.
 */
class AlertsViewModel(
    private val alerteDao: AlerteDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    /** Option de tri actuelle */
    private val _sortOption = MutableStateFlow(AlertSortOption.DATE)
    val sortOption: StateFlow<AlertSortOption> = _sortOption.asStateFlow()

    /** Filtre par gravité actuel */
    private val _gravityFilter = MutableStateFlow(AlertGravityFilter.ALL)
    val gravityFilter: StateFlow<AlertGravityFilter> = _gravityFilter.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            try {
                combine(
                    alerteDao.observeAll(),
                    _sortOption,
                    _gravityFilter
                ) { alertes, sort, filter ->
                    // Filtrer par gravité
                    val filtered = when (filter) {
                        AlertGravityFilter.ALL -> alertes
                        AlertGravityFilter.HIGH -> alertes.filter { it.gravite >= 0.7f }
                        AlertGravityFilter.MEDIUM -> alertes.filter { it.gravite in 0.4f..0.69f }
                        AlertGravityFilter.LOW -> alertes.filter { it.gravite < 0.4f }
                    }

                    // Trier
                    when (sort) {
                        AlertSortOption.DATE -> filtered.sortedByDescending { it.dateEmission }
                        AlertSortOption.GRAVITY -> filtered.sortedByDescending { it.gravite }
                        AlertSortOption.ZONE -> filtered.sortedBy { it.zone }
                    }
                }.collect { result ->
                    _uiState.value = if (result.isEmpty()) {
                        AlertsUiState.Empty
                    } else {
                        AlertsUiState.Success(result)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AlertsUiState.Error(e.message ?: "Erreur de chargement")
            }
        }
    }

    fun onSortOptionChanged(option: AlertSortOption) {
        _sortOption.value = option
    }

    fun onGravityFilterChanged(filter: AlertGravityFilter) {
        _gravityFilter.value = filter
    }

    class Factory(
        private val alerteDao: AlerteDao
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlertsViewModel(alerteDao) as T
        }
    }
}

sealed class AlertsUiState {
    data object Loading : AlertsUiState()
    data object Empty : AlertsUiState()
    data class Success(val alertes: List<AlerteEntity>) : AlertsUiState()
    data class Error(val message: String) : AlertsUiState()
}
