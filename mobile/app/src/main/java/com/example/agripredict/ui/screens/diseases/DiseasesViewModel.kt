package com.example.agripredict.ui.screens.diseases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.domain.model.Maladie
import com.example.agripredict.domain.repository.MaladieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran Base de connaissances (maladies).
 * Supporte la recherche textuelle ET le filtre par catégorie de plante.
 */
class DiseasesViewModel(
    private val repository: MaladieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Filtre par catégorie de plante (null = toutes) */
    private val _selectedPlant = MutableStateFlow<String?>(null)
    val selectedPlant: StateFlow<String?> = _selectedPlant.asStateFlow()

    /** Liste des plantes disponibles pour les FilterChips */
    private val _availablePlants = MutableStateFlow<List<String>>(emptyList())
    val availablePlants: StateFlow<List<String>> = _availablePlants.asStateFlow()

    private val _uiState = MutableStateFlow<DiseasesUiState>(DiseasesUiState.Loading)
    val uiState: StateFlow<DiseasesUiState> = _uiState.asStateFlow()

    private val _selectedMaladie = MutableStateFlow<Maladie?>(null)
    val selectedMaladie: StateFlow<Maladie?> = _selectedMaladie.asStateFlow()

    init {
        loadMaladies()
    }

    private fun loadMaladies() {
        viewModelScope.launch {
            try {
                combine(
                    repository.observeAllMaladies(),
                    _searchQuery,
                    _selectedPlant
                ) { maladies, query, plantFilter ->
                    // Extraire les plantes disponibles
                    val plants = maladies.map { extractPlantName(it.nomCommun) }.distinct().sorted()
                    _availablePlants.value = plants

                    // Appliquer filtre par plante
                    var filtered = if (plantFilter != null) {
                        maladies.filter { extractPlantName(it.nomCommun) == plantFilter }
                    } else {
                        maladies
                    }

                    // Appliquer filtre par recherche textuelle
                    if (query.isNotBlank()) {
                        filtered = filtered.filter { m ->
                            m.nomCommun.contains(query, ignoreCase = true) ||
                                    m.nomScientifique.contains(query, ignoreCase = true) ||
                                    m.description.contains(query, ignoreCase = true)
                        }
                    }

                    filtered
                }.collect { filtered ->
                    _uiState.value = if (filtered.isEmpty()) {
                        DiseasesUiState.Empty
                    } else {
                        DiseasesUiState.Success(filtered)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DiseasesUiState.Error(e.message ?: "Erreur de chargement")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onPlantFilterChanged(plant: String?) {
        _selectedPlant.value = plant
    }

    fun selectMaladie(maladie: Maladie?) {
        _selectedMaladie.value = maladie
    }

    class Factory(
        private val repository: MaladieRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DiseasesViewModel(repository) as T
        }
    }
}

/** Extrait le nom de la plante à partir du nom commun de la maladie. */
fun extractPlantName(nomCommun: String): String {
    return when {
        nomCommun.contains("manioc", ignoreCase = true) || nomCommun.contains("cassava", ignoreCase = true) -> "Manioc"
        nomCommun.contains("maïs", ignoreCase = true) || nomCommun.contains("corn", ignoreCase = true) -> "Maïs"
        nomCommun.contains("tomate", ignoreCase = true) || nomCommun.contains("tomato", ignoreCase = true) -> "Tomate"
        nomCommun.contains("poivron", ignoreCase = true) || nomCommun.contains("pepper", ignoreCase = true) -> "Poivron"
        nomCommun.contains("pomme de terre", ignoreCase = true) || nomCommun.contains("potato", ignoreCase = true) -> "Pomme de terre"
        else -> "Autres"
    }
}

sealed class DiseasesUiState {
    data object Loading : DiseasesUiState()
    data object Empty : DiseasesUiState()
    data class Success(val maladies: List<Maladie>) : DiseasesUiState()
    data class Error(val message: String) : DiseasesUiState()
}
