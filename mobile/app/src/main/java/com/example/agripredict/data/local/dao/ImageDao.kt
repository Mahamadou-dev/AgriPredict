package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : ImageLocal
 *
 * Opérations CRUD sur les images de diagnostic.
 */
@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: ImageEntity)

    @Update
    suspend fun update(image: ImageEntity)

    @Delete
    suspend fun delete(image: ImageEntity)

    @Query("SELECT * FROM image_local WHERE id = :id")
    suspend fun getById(id: String): ImageEntity?

    @Query("SELECT * FROM image_local")
    suspend fun getAll(): List<ImageEntity>

    /** Observe toutes les images en temps réel */
    @Query("SELECT * FROM image_local ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ImageEntity>>

    /** Récupère les images d'un diagnostic */
    @Query("SELECT * FROM image_local WHERE diagnosticId = :diagnosticId")
    suspend fun getByDiagnosticId(diagnosticId: String): List<ImageEntity>
}

