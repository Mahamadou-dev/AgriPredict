package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.PredictionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : PredictionLocal
 *
 * Opérations CRUD sur les résultats de prédiction IA.
 */
@Dao
interface PredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: PredictionEntity)

    @Update
    suspend fun update(prediction: PredictionEntity)

    @Delete
    suspend fun delete(prediction: PredictionEntity)

    @Query("SELECT * FROM prediction_local WHERE id = :id")
    suspend fun getById(id: String): PredictionEntity?

    @Query("SELECT * FROM prediction_local")
    suspend fun getAll(): List<PredictionEntity>

    /** Observe toutes les prédictions en temps réel */
    @Query("SELECT * FROM prediction_local ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PredictionEntity>>

    /** Récupère la prédiction d'un diagnostic */
    @Query("SELECT * FROM prediction_local WHERE diagnosticId = :diagnosticId")
    suspend fun getByDiagnosticId(diagnosticId: String): PredictionEntity?
}

