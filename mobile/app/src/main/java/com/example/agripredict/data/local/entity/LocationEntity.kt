package com.example.agripredict.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Table : LocationLocal
 *
 * Stocke les coordonnées GPS associées à un diagnostic.
 */
@Entity(tableName = "location_local")
data class LocationEntity(
    @PrimaryKey
    val id: String,                // UUID
    val latitude: Double,
    val longitude: Double,
    val region: String = "",
    val village: String = ""
)

