package com.example.agripredict.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.agripredict.data.local.dao.*
import com.example.agripredict.data.local.entity.*

/**
 * Base de données Room principale de AgriPredict.
 *
 * Contient toutes les tables pour le stockage local offline-first.
 * Version 1 = schéma initial.
 *
 * Tables :
 *   - utilisateur_local   → profil agriculteur
 *   - diagnostic_local     → diagnostics effectués
 *   - image_local          → images capturées
 *   - location_local       → coordonnées GPS
 *   - prediction_local     → résultats IA
 *   - maladie_local        → base de connaissances maladies
 *   - traitement_local     → traitements recommandés
 *   - alerte_local         → alertes agricoles
 *   - modele_ia_local      → modèles TFLite installés
 */
@Database(
    entities = [
        UserEntity::class,
        DiagnosticEntity::class,
        ImageEntity::class,
        LocationEntity::class,
        PredictionEntity::class,
        MaladieEntity::class,
        TraitementEntity::class,
        AlerteEntity::class,
        ModeleIAEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AgriPredictDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun diagnosticDao(): DiagnosticDao
    abstract fun imageDao(): ImageDao
    abstract fun locationDao(): LocationDao
    abstract fun predictionDao(): PredictionDao
    abstract fun maladieDao(): MaladieDao
    abstract fun traitementDao(): TraitementDao
    abstract fun alerteDao(): AlerteDao
    abstract fun modeleIADao(): ModeleIADao
}

