package com.example.agripredict.ui.screens.diagnostic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agripredict.domain.model.DiagnosticResult
import com.example.agripredict.domain.usecase.SaveDiagnosticUseCase
import com.example.agripredict.util.LabelFormatter
import com.example.agripredict.util.TFLiteClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * ViewModel pour l'écran de diagnostic IA.
 *
 * Gère le flux complet :
 * 1. Capture photo (caméra OU galerie)
 * 2. Analyse IA via TFLite
 * 3. Affichage résultat formaté
 * 4. Sauvegarde dans Room
 *
 * Suit le pattern MVVM : l'UI observe le StateFlow et réagit aux changements.
 */
class DiagnosticViewModel(
    private val classifier: TFLiteClassifier,
    private val saveDiagnosticUseCase: SaveDiagnosticUseCase,
    private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "DiagnosticViewModel"
    }

    // === État observable par l'UI ===
    private val _uiState = MutableStateFlow<DiagnosticUiState>(DiagnosticUiState.Idle)
    val uiState: StateFlow<DiagnosticUiState> = _uiState.asStateFlow()

    // Données temporaires du diagnostic en cours
    private var currentBitmap: Bitmap? = null
    private var currentImagePath: String = ""
    private var currentResult: TFLiteClassifier.ClassificationResult? = null

    // ==========================================
    // Actions utilisateur
    // ==========================================

    /**
     * Appelée quand l'utilisateur a pris une photo via la caméra.
     * Sauvegarde le bitmap en fichier et passe à l'état ImageCaptured.
     */
    fun onPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentBitmap = bitmap
                currentImagePath = saveBitmapToFile(bitmap)
                Log.d(TAG, "📷 Photo caméra sauvegardée : $currentImagePath")

                _uiState.value = DiagnosticUiState.ImageCaptured(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur sauvegarde photo : ${e.message}")
                _uiState.value = DiagnosticUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    /**
     * Appelée quand l'utilisateur a choisi une image depuis la galerie.
     * Décode l'URI en Bitmap, sauvegarde en fichier interne et passe à ImageCaptured.
     */
    fun onImagePickedFromGallery(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Décoder l'URI en Bitmap
                val inputStream = appContext.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap == null) {
                    _uiState.value = DiagnosticUiState.Error("Impossible de lire l'image")
                    return@launch
                }

                currentBitmap = bitmap
                currentImagePath = saveBitmapToFile(bitmap)
                Log.d(TAG, "🖼️ Image galerie sauvegardée : $currentImagePath")

                _uiState.value = DiagnosticUiState.ImageCaptured(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lecture galerie : ${e.message}")
                _uiState.value = DiagnosticUiState.Error(e.message ?: "Erreur lecture image")
            }
        }
    }

    /**
     * Lance l'analyse IA sur l'image capturée.
     * Exécute le classifieur TFLite dans un thread de calcul.
     */
    fun analyzeImage() {
        val bitmap = currentBitmap ?: return

        viewModelScope.launch(Dispatchers.Default) {
            _uiState.value = DiagnosticUiState.Analyzing

            try {
                // Exécuter la classification IA
                val result = classifier.classify(bitmap)
                currentResult = result

                // Formater le label brut en texte lisible
                val formatted = LabelFormatter.format(result.label)

                Log.d(TAG, "🔬 Analyse : ${formatted.plant} → ${formatted.disease} (${result.confidence})")

                _uiState.value = DiagnosticUiState.ResultReady(
                    bitmap = bitmap,
                    rawLabel = result.label,
                    plantName = formatted.plant,
                    diseaseName = formatted.disease,
                    isHealthy = formatted.isHealthy,
                    confidence = result.confidence,
                    isDemo = result.isDemo
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur analyse IA : ${e.message}")
                _uiState.value = DiagnosticUiState.Error(e.message ?: "Erreur d'analyse")
            }
        }
    }

    /**
     * Sauvegarde le diagnostic complet dans la base locale Room.
     */
    fun saveDiagnostic() {
        val result = currentResult ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val diagnosticResult = DiagnosticResult(
                    id = UUID.randomUUID().toString(),
                    userId = "local_user", // TODO: Remplacer par l'utilisateur connecté (Phase 6)
                    date = System.currentTimeMillis(),
                    label = result.label,
                    confidence = result.confidence,
                    modelVersion = if (result.isDemo) "demo" else "v1.0",
                    imagePath = currentImagePath
                )

                saveDiagnosticUseCase(diagnosticResult)
                Log.d(TAG, "💾 Diagnostic sauvegardé : ${diagnosticResult.id}")

                _uiState.value = DiagnosticUiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur sauvegarde : ${e.message}")
                _uiState.value = DiagnosticUiState.Error(e.message ?: "Erreur de sauvegarde")
            }
        }
    }

    /**
     * Remet l'écran à l'état initial pour un nouveau diagnostic.
     */
    fun reset() {
        currentBitmap = null
        currentImagePath = ""
        currentResult = null
        _uiState.value = DiagnosticUiState.Idle
    }

    // ==========================================
    // Utilitaires
    // ==========================================

    /**
     * Sauvegarde un Bitmap en fichier JPEG dans le stockage interne.
     */
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val dir = File(appContext.filesDir, "diagnostics")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "diag_${UUID.randomUUID()}.jpg"
        val file = File(dir, fileName)

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }

        return file.absolutePath
    }

    // ==========================================
    // Factory pour injection manuelle
    // ==========================================

    class Factory(
        private val classifier: TFLiteClassifier,
        private val saveDiagnosticUseCase: SaveDiagnosticUseCase,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiagnosticViewModel::class.java)) {
                return DiagnosticViewModel(classifier, saveDiagnosticUseCase, appContext) as T
            }
            throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
        }
    }
}

// ==========================================
// États de l'écran de diagnostic
// ==========================================

/**
 * Sealed class représentant tous les états possibles de l'écran diagnostic.
 *
 * Idle → ImageCaptured → Analyzing → ResultReady → Saved
 *                                                 ↘ Error
 */
sealed class DiagnosticUiState {
    /** État initial : aucune image sélectionnée */
    data object Idle : DiagnosticUiState()

    /** Une image a été capturée/sélectionnée, prête pour l'analyse */
    data class ImageCaptured(val bitmap: Bitmap) : DiagnosticUiState()

    /** Analyse IA en cours */
    data object Analyzing : DiagnosticUiState()

    /** Résultat de l'analyse disponible */
    data class ResultReady(
        val bitmap: Bitmap,
        val rawLabel: String,
        val plantName: String,
        val diseaseName: String,
        val isHealthy: Boolean,
        val confidence: Float,
        val isDemo: Boolean = false
    ) : DiagnosticUiState()

    /** Diagnostic sauvegardé avec succès */
    data object Saved : DiagnosticUiState()

    /** Erreur survenue */
    data class Error(val message: String) : DiagnosticUiState()
}
