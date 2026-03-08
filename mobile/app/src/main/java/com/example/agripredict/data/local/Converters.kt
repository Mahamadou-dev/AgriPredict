package com.example.agripredict.data.local

import androidx.room.TypeConverter
import com.example.agripredict.sync.SyncStatus

/**
 * Convertisseurs de types pour Room.
 *
 * Room ne peut pas stocker directement des enums dans SQLite.
 * Ces convertisseurs transforment SyncStatus ↔ String automatiquement.
 */
class Converters {

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}

