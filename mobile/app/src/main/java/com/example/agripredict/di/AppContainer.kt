package com.example.agripredict.di

import android.content.Context
import androidx.room.Room
import com.example.agripredict.data.local.AgriPredictDatabase
import com.example.agripredict.data.preferences.LanguagePreferences
import com.example.agripredict.data.repository.DiagnosticRepositoryImpl
import com.example.agripredict.domain.repository.DiagnosticRepository
import com.example.agripredict.domain.usecase.GetDiagnosticsUseCase
import com.example.agripredict.domain.usecase.SaveDiagnosticUseCase
import com.example.agripredict.sync.NetworkChecker
import com.example.agripredict.sync.SyncManager
import com.example.agripredict.ui.screens.diagnostic.DiagnosticViewModel
import com.example.agripredict.util.TFLiteClassifier

/**
 * Conteneur d'injection de dépendances simple.
 *
 * Pour un projet de niveau licence, on utilise un conteneur manuel
 * au lieu de Hilt/Dagger (trop complexe pour une présentation jury).
 *
 * Ce conteneur crée et fournit toutes les dépendances de l'application.
 * Il est initialisé une seule fois dans AgriPredictApplication.
 */
class AppContainer(private val context: Context) {

    // === Base de données Room ===
    val database: AgriPredictDatabase by lazy {
        Room.databaseBuilder(
            context,
            AgriPredictDatabase::class.java,
            "agripredict_database"
        ).build()
    }

    // === DAOs (un par table) ===
    val userDao by lazy { database.userDao() }
    val diagnosticDao by lazy { database.diagnosticDao() }
    val imageDao by lazy { database.imageDao() }
    val locationDao by lazy { database.locationDao() }
    val predictionDao by lazy { database.predictionDao() }
    val maladieDao by lazy { database.maladieDao() }
    val traitementDao by lazy { database.traitementDao() }
    val alerteDao by lazy { database.alerteDao() }
    val modeleIADao by lazy { database.modeleIADao() }

    // === Repositories ===
    val diagnosticRepository: DiagnosticRepository by lazy {
        DiagnosticRepositoryImpl(diagnosticDao, imageDao, predictionDao)
    }

    // === Use Cases ===
    val getDiagnosticsUseCase by lazy { GetDiagnosticsUseCase(diagnosticRepository) }
    val saveDiagnosticUseCase by lazy { SaveDiagnosticUseCase(diagnosticRepository) }

    // === IA : Classifieur TensorFlow Lite ===
    val tfliteClassifier by lazy { TFLiteClassifier(context) }

    // === Préférences ===
    val languagePreferences by lazy { LanguagePreferences(context) }

    // === Synchronisation ===
    val networkChecker by lazy { NetworkChecker(context) }
    val syncManager by lazy { SyncManager(networkChecker) }

    // === ViewModels Factories ===

    /**
     * Factory pour créer le DiagnosticViewModel avec ses dépendances.
     * Utilisé dans le NavGraph via viewModel(factory = ...).
     */
    val diagnosticViewModelFactory by lazy {
        DiagnosticViewModel.Factory(
            classifier = tfliteClassifier,
            saveDiagnosticUseCase = saveDiagnosticUseCase,
            appContext = context
        )
    }
}

