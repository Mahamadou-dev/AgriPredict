package com.example.agripredict

import android.app.Application
import com.example.agripredict.di.AppContainer

/**
 * Classe Application principale de AgriPredict.
 *
 * Initialise le conteneur de dépendances au démarrage.
 * Doit être déclarée dans AndroidManifest.xml.
 */
class AgriPredictApplication : Application() {

    /** Conteneur de dépendances accessible depuis toute l'application */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialise le conteneur DI avec le contexte application
        container = AppContainer(this)
    }
}

