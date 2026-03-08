package com.example.agripredict.sync

/**
 * Interface du repository de synchronisation.
 *
 * Définit les opérations de sync entre local et distant.
 * TODO: Implémenter dans data/repository/
 */
interface SyncRepository {

    /** Synchronise les diagnostics en attente (uplink) */
    suspend fun syncPendingDiagnostics(): Boolean

    /** Synchronise le profil utilisateur (uplink) */
    suspend fun syncUserProfile(): Boolean

    /** Vérifie les mises à jour disponibles (downlink) */
    suspend fun checkForUpdates(): Boolean

    /** Télécharge et applique les mises à jour (downlink) */
    suspend fun downloadAndApplyUpdates(): Boolean
}

