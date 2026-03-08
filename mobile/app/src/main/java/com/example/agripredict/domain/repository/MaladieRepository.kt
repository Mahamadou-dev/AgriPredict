package com.example.agripredict.domain.repository

import com.example.agripredict.domain.model.Maladie
import kotlinx.coroutines.flow.Flow

/**
 * Interface du repository des maladies (base de connaissances).
 */
interface MaladieRepository {

    /** Observe toutes les maladies */
    fun observeAllMaladies(): Flow<List<Maladie>>

    /** Récupère une maladie par son ID */
    suspend fun getMaladieById(id: Int): Maladie?
}

