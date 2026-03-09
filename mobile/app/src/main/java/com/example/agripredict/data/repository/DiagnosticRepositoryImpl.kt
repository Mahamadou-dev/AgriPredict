package com.example.agripredict.data.repository

import com.example.agripredict.data.local.dao.DiagnosticDao
import com.example.agripredict.data.local.dao.ImageDao
import com.example.agripredict.data.local.dao.PredictionDao
import com.example.agripredict.data.local.entity.DiagnosticEntity
import com.example.agripredict.data.local.entity.ImageEntity
import com.example.agripredict.data.local.entity.PredictionEntity
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.repository.DiagnosticRepository
import com.example.agripredict.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Implémentation concrète du repository de diagnostics.
 *
 * Fait le lien entre la couche domain (modèles métier)
 * et la couche data (Room entities).
 *
 * Gère la sauvegarde complète d'un diagnostic :
 * DiagnosticEntity + ImageEntity + PredictionEntity
 */
class DiagnosticRepositoryImpl(
    private val diagnosticDao: DiagnosticDao,
    private val imageDao: ImageDao,
    private val predictionDao: PredictionDao
) : DiagnosticRepository {

    override fun observeAllDiagnostics(): Flow<List<DiagnosticResult>> {
        return diagnosticDao.observeAll().map { entities ->
            entities.map { entity -> enrichDiagnostic(entity) }
        }
    }

    override suspend fun saveDiagnostic(result: DiagnosticResult) {
        // 1. Sauvegarder l'image si présente
        val imageId = if (result.imagePath.isNotBlank()) {
            val id = UUID.randomUUID().toString()
            imageDao.insert(
                ImageEntity(
                    id = id,
                    path = result.imagePath,
                    diagnosticId = result.id
                )
            )
            id
        } else null

        // 2. Sauvegarder la prédiction IA si présente
        val predictionId = if (result.label.isNotBlank()) {
            val id = UUID.randomUUID().toString()
            predictionDao.insert(
                PredictionEntity(
                    id = id,
                    label = result.label,
                    confidence = result.confidence,
                    modelVersion = result.modelVersion,
                    diagnosticId = result.id
                )
            )
            id
        } else null

        // 3. Sauvegarder le diagnostic avec les références
        diagnosticDao.insert(
            DiagnosticEntity(
                id = result.id,
                userId = result.userId,
                date = result.date,
                syncStatus = result.syncStatus,
                imageId = imageId,
                predictionId = predictionId
            )
        )
    }

    override suspend fun getDiagnosticById(id: String): DiagnosticResult? {
        val entity = diagnosticDao.getById(id) ?: return null
        return enrichDiagnostic(entity)
    }

    override suspend fun getBySyncStatus(status: SyncStatus): List<DiagnosticResult> {
        return diagnosticDao.getBySyncStatus(status).map { enrichDiagnostic(it) }
    }

    override suspend fun updateSyncStatus(id: String, status: SyncStatus) {
        val entity = diagnosticDao.getById(id) ?: return
        diagnosticDao.update(entity.copy(syncStatus = status, updatedAt = System.currentTimeMillis()))
    }

    // ==========================================
    // Fonctions de mapping Entity → Domain
    // ==========================================

    /**
     * Enrichit un DiagnosticEntity avec ses données associées (image, prédiction).
     * Combine 3 tables en un seul modèle métier DiagnosticResult.
     */
    private suspend fun enrichDiagnostic(entity: DiagnosticEntity): DiagnosticResult {
        // Récupérer la prédiction associée
        val prediction = predictionDao.getByDiagnosticId(entity.id)
        // Récupérer les images associées
        val images = imageDao.getByDiagnosticId(entity.id)

        return DiagnosticResult(
            id = entity.id,
            userId = entity.userId,
            date = entity.date,
            syncStatus = entity.syncStatus,
            label = prediction?.label ?: "",
            confidence = prediction?.confidence ?: 0f,
            modelVersion = prediction?.modelVersion ?: "",
            imagePath = images.firstOrNull()?.path ?: ""
        )
    }
}

