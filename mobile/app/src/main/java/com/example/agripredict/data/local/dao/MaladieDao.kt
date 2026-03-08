package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.MaladieEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : MaladieLocal
 *
 * Opérations CRUD sur la base de connaissances des maladies.
 */
@Dao
interface MaladieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(maladie: MaladieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(maladies: List<MaladieEntity>)

    @Update
    suspend fun update(maladie: MaladieEntity)

    @Delete
    suspend fun delete(maladie: MaladieEntity)

    @Query("SELECT * FROM maladie_local WHERE id = :id")
    suspend fun getById(id: Int): MaladieEntity?

    @Query("SELECT * FROM maladie_local")
    suspend fun getAll(): List<MaladieEntity>

    /** Observe toutes les maladies en temps réel */
    @Query("SELECT * FROM maladie_local ORDER BY nomCommun ASC")
    fun observeAll(): Flow<List<MaladieEntity>>

    @Query("DELETE FROM maladie_local")
    suspend fun deleteAll()
}

