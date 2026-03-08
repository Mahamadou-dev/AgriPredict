package com.example.agripredict.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utilitaire pour vérifier la connectivité réseau.
 * Utilisé avant chaque tentative de synchronisation.
 */
class NetworkChecker(private val context: Context) {

    /**
     * Vérifie si l'appareil est connecté à Internet.
     * @return true si une connexion réseau est disponible
     */
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

