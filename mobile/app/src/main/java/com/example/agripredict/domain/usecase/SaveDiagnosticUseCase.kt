package com.example.agripredict.domain.usecase

import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.repository.DiagnosticRepository

/**
 * Cas d'utilisation : Sauvegarder un diagnostic.
 */
class SaveDiagnosticUseCase(
    private val repository: DiagnosticRepository
) {
    /** Sauvegarde un résultat de diagnostic dans la base locale */
    suspend operator fun invoke(result: DiagnosticResult) {
        repository.saveDiagnostic(result)
    }
}

