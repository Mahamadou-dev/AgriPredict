package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.TraitementEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : TraitementLocal
 *
 * Opérations CRUD sur les traitements recommandés.
 */
@Dao
interface TraitementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(traitement: TraitementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(traitements: List<TraitementEntity>)

    @Update
    suspend fun update(traitement: TraitementEntity)

    @Delete
    suspend fun delete(traitement: TraitementEntity)

    @Query("SELECT * FROM traitement_local WHERE id = :id")
    suspend fun getById(id: Int): TraitementEntity?

    @Query("SELECT * FROM traitement_local")
    suspend fun getAll(): List<TraitementEntity>

    /** Observe tous les traitements en temps réel */
    @Query("SELECT * FROM traitement_local")
    fun observeAll(): Flow<List<TraitementEntity>>

    /** Récupère les traitements d'une maladie */
    @Query("SELECT * FROM traitement_local WHERE maladieId = :maladieId")
    suspend fun getByMaladieId(maladieId: Int): List<TraitementEntity>

    @Query("DELETE FROM traitement_local")
    suspend fun deleteAll()
}

