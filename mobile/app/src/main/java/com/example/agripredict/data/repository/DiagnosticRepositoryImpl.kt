package com.example.agripredict.data.repository

import com.example.agripredict.data.local.dao.DiagnosticDao
import com.example.agripredict.data.local.entity.DiagnosticEntity
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.repository.DiagnosticRepository
import com.example.agripredict.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implémentation concrète du repository de diagnostics.
 *
 * Fait le lien entre la couche domain (modèles métier)
 * et la couche data (Room entities).
 *
 * Note : pour l'instant, seul DiagnosticEntity est mappé.
 * Les données image/localisation/prédiction seront ajoutées
 * quand les fonctionnalités correspondantes seront implémentées.
 */
class DiagnosticRepositoryImpl(
    private val diagnosticDao: DiagnosticDao
) : DiagnosticRepository {

    override fun observeAllDiagnostics(): Flow<List<DiagnosticResult>> {
        return diagnosticDao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveDiagnostic(result: DiagnosticResult) {
        diagnosticDao.insert(result.toEntity())
    }

    override suspend fun getDiagnosticById(id: String): DiagnosticResult? {
        return diagnosticDao.getById(id)?.toDomainModel()
    }

    override suspend fun getBySyncStatus(status: SyncStatus): List<DiagnosticResult> {
        return diagnosticDao.getBySyncStatus(status).map { it.toDomainModel() }
    }

    override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        val entity = diagnosticDao.getById(id) ?: return
        diagnosticDao.update(entity.copy(syncStatus = status, updatedAt = System.currentTimeMillis()))
    }
}

// === Fonctions de mapping Entity <-> Domain ===

/** Convertit une entité Room en modèle métier */
private fun DiagnosticEntity.toDomainModel() = DiagnosticResult(
    id = id,
    userId = userId,
    date = date,
    syncStatus = syncStatus
    // TODO: Ajouter image/prediction/location quand implémentés
)

/** Convertit un modèle métier en entité Room */
private fun DiagnosticResult.toEntity() = DiagnosticEntity(
    id = id,
    userId = userId,
    date = date,
    syncStatus = syncStatus
)

