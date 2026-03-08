package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.DiagnosticEntity
import com.example.agripredict.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO : DiagnosticLocal
 *
 * Opérations CRUD complètes sur la table diagnostic.
 */
@Dao
interface DiagnosticDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diagnostic: DiagnosticEntity)

    @Update
    suspend fun update(diagnostic: DiagnosticEntity)

    @Delete
    suspend fun delete(diagnostic: DiagnosticEntity)

    @Query("SELECT * FROM diagnostic_local WHERE id = :id")
    suspend fun getById(id: String): DiagnosticEntity?

    @Query("SELECT * FROM diagnostic_local ORDER BY date DESC")
    suspend fun getAll(): List<DiagnosticEntity>

    /** Observe tous les diagnostics (du plus récent au plus ancien) */
    @Query("SELECT * FROM diagnostic_local ORDER BY date DESC")
    fun observeAll(): Flow<List<DiagnosticEntity>>

    /** Observe les diagnostics d'un utilisateur */
    @Query("SELECT * FROM diagnostic_local WHERE userId = :userId ORDER BY date DESC")
    fun observeByUser(userId: String): Flow<List<DiagnosticEntity>>

    /** Récupère les diagnostics par statut de sync (ex: PENDING) */
    @Query("SELECT * FROM diagnostic_local WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: SyncStatus): List<DiagnosticEntity>
}

