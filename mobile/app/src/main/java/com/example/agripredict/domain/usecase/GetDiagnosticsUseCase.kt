package com.example.agripredict.domain.usecase

import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.repository.DiagnosticRepository
import kotlinx.coroutines.flow.Flow

/**
 * Cas d'utilisation : Récupérer l'historique des diagnostics.
 *
 * Respecte le pattern UseCase de Clean Architecture.
 * Chaque UseCase = une seule responsabilité.
 */
class GetDiagnosticsUseCase(
    private val repository: DiagnosticRepository
) {
    /** Retourne un Flow observant tous les diagnostics */
    operator fun invoke(): Flow<List<DiagnosticResult>> {
        return repository.observeAllDiagnostics()
    }
}

