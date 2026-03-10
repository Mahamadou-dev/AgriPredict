package com.example.agripredict.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore séparé pour la session (ne pas mélanger avec les préférences langue)
private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_preferences"
)

/**
 * Gestion de la session utilisateur avec DataStore.
 *
 * Persiste l'état de connexion entre les lancements de l'app.
 * Stocke : userId, userName, isLoggedIn.
 */
class SessionPreferences(private val context: Context) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }

    /** Observe si l'utilisateur est connecté */
    val isLoggedIn: Flow<Boolean> = context.sessionDataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] ?: false
    }

    /** Observe l'ID de l'utilisateur connecté */
    val userId: Flow<String> = context.sessionDataStore.data.map { prefs ->
        prefs[USER_ID_KEY] ?: ""
    }

    /** Observe le nom de l'utilisateur connecté */
    val userName: Flow<String> = context.sessionDataStore.data.map { prefs ->
        prefs[USER_NAME_KEY] ?: ""
    }

    /**
     * Récupère l'ID de l'utilisateur connecté (suspend).
     * Retourne null si pas de session.
     */
    suspend fun getUserId(): String? {
        val id = userId.first()
        return id.ifEmpty { null }
    }

    /**
     * Sauvegarde la session après connexion/inscription.
     */
    suspend fun saveSession(userId: String, userName: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = userName
            prefs[IS_LOGGED_IN_KEY] = true
        }
    }

    /**
     * Efface la session (déconnexion).
     */
    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

