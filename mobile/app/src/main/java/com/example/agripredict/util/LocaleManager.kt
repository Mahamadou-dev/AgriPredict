package com.example.agripredict.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Gestionnaire de langue de l'application.
 *
 * Permet de changer dynamiquement la langue de l'interface.
 * Compatible avec Android 7+ (API 24+) grâce à LocaleList.
 *
 * Sur Android 13+ (Tiramisu), utilise l'API Per-App Language native.
 * Sur les versions antérieures, met à jour la configuration de la locale.
 */
object LocaleManager {

    /** Langues supportées par AgriPredict */
    val supportedLanguages = listOf("fr", "en", "ha", "dje")

    /**
     * Change la langue de l'application.
     *
     * @param context Contexte Android
     * @param languageCode Code de la langue (ex: "fr", "en", "ha", "dje")
     */
    fun setLocale(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ : utilise l'API Per-App Language native
            val localeManager = context.getSystemService(
                android.app.LocaleManager::class.java
            )
            localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
        } else {
            // Android < 13 : met à jour la configuration locale
            @Suppress("DEPRECATION")
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocales(LocaleList(locale))
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }

    /**
     * Récupère la langue actuelle de l'application.
     *
     * @return Le code de la langue actuelle (ex: "fr", "en")
     */
    fun getCurrentLocale(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(
                android.app.LocaleManager::class.java
            )
            val locales = localeManager.applicationLocales
            if (locales.isEmpty) Locale.getDefault().language
            else locales[0]?.language ?: "fr"
        } else {
            Locale.getDefault().language
        }
    }
}

