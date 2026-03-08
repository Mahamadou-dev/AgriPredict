package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.AlerteEntity
import com.example.agripredict.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO : AlerteLocal
 *
 * Opérations CRUD sur les alertes agricoles.
 */
@Dao
interface AlerteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alerte: AlerteEntity)

    @Update
    suspend fun update(alerte: AlerteEntity)

    @Delete
    suspend fun delete(alerte: AlerteEntity)

    @Query("SELECT * FROM alerte_local WHERE id = :id")
    suspend fun getById(id: String): AlerteEntity?

    @Query("SELECT * FROM alerte_local ORDER BY dateEmission DESC")
    suspend fun getAll(): List<AlerteEntity>

    /** Observe toutes les alertes en temps réel (plus récentes en premier) */
    @Query("SELECT * FROM alerte_local ORDER BY dateEmission DESC")
    fun observeAll(): Flow<List<AlerteEntity>>

    /** Récupère les alertes par statut de sync */
    @Query("SELECT * FROM alerte_local WHERE syncStatus = :status")
    suspend fun getBySyncStatus(status: SyncStatus): List<AlerteEntity>
}

