package com.example.agripredict.sync

/**
 * État de synchronisation d'un enregistrement local.
 *
 * Chaque entité qui doit être synchronisée avec le serveur
 * possède un champ syncStatus de ce type.
 *
 * PENDING  → En attente d'envoi vers le serveur
 * SYNCED   → Déjà synchronisé avec le serveur
 * FAILED   → Échec de synchronisation (sera réessayé)
 */
enum class SyncStatus {
    PENDING,
    SYNCED,
    FAILED
}

/**
 * Direction de la synchronisation.
 *
 * UPLINK   → Envoi local → serveur
 * DOWNLINK → Réception serveur → local
 */
enum class SyncType {
    UPLINK,
    DOWNLINK
}

