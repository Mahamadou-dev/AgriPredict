package com.example.agripredict.data.local.dao

import androidx.room.*
import com.example.agripredict.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO : UtilisateurLocal
 *
 * Opérations CRUD complètes sur la table utilisateur.
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT * FROM utilisateur_local WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM utilisateur_local")
    suspend fun getAll(): List<UserEntity>

    /** Recherche un utilisateur par numéro de téléphone (pour le login) */
    @Query("SELECT * FROM utilisateur_local WHERE telephone = :telephone")
    suspend fun getByTelephone(telephone: String): UserEntity?

    /** Vérifie si un téléphone est déjà enregistré */
    @Query("SELECT COUNT(*) FROM utilisateur_local WHERE telephone = :telephone")
    suspend fun countByTelephone(telephone: String): Int

    /** Observe tous les utilisateurs en temps réel */
    @Query("SELECT * FROM utilisateur_local")
    fun observeAll(): Flow<List<UserEntity>>
}

