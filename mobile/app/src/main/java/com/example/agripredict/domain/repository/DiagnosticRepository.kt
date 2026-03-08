package com.example.agripredict.domain.repository

import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface du repository de diagnostics.
 *
 * Définit le contrat entre la couche domain et la couche data.
 * L'implémentation concrète est dans data/repository/.
 */
interface DiagnosticRepository {

    /** Observe tous les diagnostics (réactif) */
    fun observeAllDiagnostics(): Flow<List<DiagnosticResult>>

    /** Sauvegarde un nouveau diagnostic */
    suspend fun saveDiagnostic(result: DiagnosticResult)

    /** Récupère un diagnostic par ID */
    suspend fun getDiagnosticById(id: String): DiagnosticResult?

    /** Récupère les diagnostics par statut de sync (ex: PENDING) */
    suspend fun getBySyncStatus(status: SyncStatus): List<DiagnosticResult>

    /** Met à jour le statut de synchronisation d'un diagnostic */
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
}

