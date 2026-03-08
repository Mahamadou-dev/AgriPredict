package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.ModeleIAEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : ModeleIALocal
 *
 * Opérations CRUD sur les modèles IA installés.
 */
@Dao
interface ModeleIADao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(modele: ModeleIAEntity)

    @Update
    suspend fun update(modele: ModeleIAEntity)

    @Delete
    suspend fun delete(modele: ModeleIAEntity)

    @Query("SELECT * FROM modele_ia_local WHERE version = :version")
    suspend fun getById(version: String): ModeleIAEntity?

    @Query("SELECT * FROM modele_ia_local")
    suspend fun getAll(): List<ModeleIAEntity>

    /** Observe tous les modèles installés */
    @Query("SELECT * FROM modele_ia_local ORDER BY installedAt DESC")
    fun observeAll(): Flow<List<ModeleIAEntity>>

    /** Récupère le modèle le plus récent */
    @Query("SELECT * FROM modele_ia_local ORDER BY installedAt DESC LIMIT 1")
    suspend fun getLatest(): ModeleIAEntity?
}

