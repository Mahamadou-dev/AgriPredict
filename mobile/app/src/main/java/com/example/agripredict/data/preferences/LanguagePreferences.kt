package com.example.agripredict.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension pour créer un DataStore unique
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "agripredict_preferences"
)

/**
 * Gestion des préférences de langue avec DataStore.
 *
 * Stocke la langue choisie par l'utilisateur de manière persistante.
 */
class LanguagePreferences(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        const val DEFAULT_LANGUAGE = "fr"
    }

    /** Observe la langue sélectionnée (réactif avec Flow) */
    val selectedLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
    }

    /** Sauvegarde la langue choisie */
    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = languageCode
        }
    }
}

