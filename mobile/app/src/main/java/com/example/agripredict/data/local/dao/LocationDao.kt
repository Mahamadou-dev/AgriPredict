package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : LocationLocal
 *
 * Opérations CRUD sur les localisations GPS.
 */
@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("SELECT * FROM location_local WHERE id = :id")
    suspend fun getById(id: String): LocationEntity?

    @Query("SELECT * FROM location_local")
    suspend fun getAll(): List<LocationEntity>

    /** Observe toutes les localisations en temps réel */
    @Query("SELECT * FROM location_local")
    fun observeAll(): Flow<List<LocationEntity>>
}

