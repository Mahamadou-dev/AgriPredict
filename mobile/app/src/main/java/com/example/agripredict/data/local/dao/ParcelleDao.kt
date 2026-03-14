package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.ParcelleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : ParcelleLocal
 *
 * Opérations CRUD sur les parcelles agricoles.
 */
@Dao
interface ParcelleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(parcelle: ParcelleEntity)

    @Update
    suspend fun update(parcelle: ParcelleEntity)

    @Delete
    suspend fun delete(parcelle: ParcelleEntity)

    @Query("SELECT * FROM parcelle_local WHERE id = :id")
    suspend fun getById(id: String): ParcelleEntity?

    @Query("SELECT * FROM parcelle_local WHERE utilisateurId = :utilisateurId ORDER BY nomParcelle ASC")
    suspend fun getByUserId(utilisateurId: String): List<ParcelleEntity>

    /** Observe les parcelles d'un utilisateur en temps réel */
    @Query("SELECT * FROM parcelle_local WHERE utilisateurId = :utilisateurId ORDER BY nomParcelle ASC")
    fun observeByUser(utilisateurId: String): Flow<List<ParcelleEntity>>

    /** Compte le nombre de parcelles d'un utilisateur */
    @Query("SELECT COUNT(*) FROM parcelle_local WHERE utilisateurId = :utilisateurId")
    suspend fun countByUser(utilisateurId: String): Int
}

