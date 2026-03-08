package com.example.agripredict.data.remote.api

import com.example.agripredict.data.remote.dto.downlink.CheckUpdatesResponseDTO
import com.example.agripredict.data.remote.dto.downlink.UpdateBundleDTO
import com.example.agripredict.data.remote.dto.uplink.DiagnosticUploadDTO
import com.example.agripredict.data.remote.dto.uplink.DiagnosticUploadResponseDTO
import com.example.agripredict.data.remote.dto.uplink.UserSyncDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interface Retrofit définissant les endpoints de l'API AgriPredict.
 *
 * UPLINK = envoi de données vers le serveur.
 * DOWNLINK = réception de données depuis le serveur.
 */
interface AgriPredictApi {

    // ========== UPLINK ==========

    /** Synchronise le profil utilisateur vers le serveur */
    @POST("api/users/sync")
    suspend fun syncUser(@Body user: UserSyncDTO): Unit

    /** Envoie un diagnostic au serveur */
    @POST("api/diagnostics/upload")
    suspend fun uploadDiagnostic(@Body diagnostic: DiagnosticUploadDTO): DiagnosticUploadResponseDTO

    // ========== DOWNLINK ==========

    /** Vérifie s'il y a des mises à jour disponibles */
    @GET("api/updates/check")
    suspend fun checkUpdates(
        @Query("knowledgeVersion") knowledgeVersion: String = "",
        @Query("modelVersion") modelVersion: String = ""
    ): CheckUpdatesResponseDTO

    /** Télécharge les mises à jour disponibles */
    @GET("api/updates/download")
    suspend fun downloadUpdates(): UpdateBundleDTO
}

