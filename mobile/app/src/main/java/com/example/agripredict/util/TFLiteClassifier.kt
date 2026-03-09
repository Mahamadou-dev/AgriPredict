package com.example.agripredict.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Classifieur TensorFlow Lite pour le diagnostic de maladies des plantes.
 *
 * Supporte les modèles FLOAT32 et INT8 (quantifié).
 * Détecte automatiquement le type d'entrée/sortie du modèle.
 *
 * Modèle utilisé : MobileNetV2 INT8 (2.87 Mo, 24 classes, ~82% accuracy).
 *
 * Si le modèle n'est pas disponible, fonctionne en mode DÉMO
 * avec des résultats simulés (utile pour le développement).
 */
class TFLiteClassifier(private val context: Context) {

    companion object {
        private const val TAG = "TFLiteClassifier"

        /** Nom du fichier modèle dans assets/ */
        private const val MODEL_FILENAME = "plant_disease_model.tflite"

        /** Nom du fichier labels dans assets/ */
        private const val LABELS_FILENAME = "labels.txt"

        /** Taille d'entrée du modèle (224x224 pixels, standard ImageNet) */
        const val INPUT_SIZE = 224

        /** Nombre de canaux couleur (RGB) */
        private const val CHANNELS = 3
    }

    /** Interpréteur TFLite — null si le modèle n'est pas disponible */
    private var interpreter: Interpreter? = null

    /** Liste des labels (maladies) lues depuis labels.txt */
    private var labels: List<String> = emptyList()

    /** Type de données d'entrée du modèle (FLOAT32 ou UINT8) */
    private var inputDataType: DataType = DataType.FLOAT32

    /** Type de données de sortie du modèle (FLOAT32 ou UINT8) */
    private var outputDataType: DataType = DataType.FLOAT32

    /** Nombre de classes en sortie (lu depuis le modèle) */
    private var outputClassCount: Int = 0

    /** True si le modèle est chargé, false = mode démo */
    val isModelLoaded: Boolean get() = interpreter != null

    init {
        loadLabels()
        loadModel()
    }

    // ==========================================
    // Chargement des ressources
    // ==========================================

    /**
     * Charge les labels depuis assets/labels.txt.
     * Les labels doivent correspondre exactement aux classes du modèle
     * et dans le même ordre que class_names.json de l'entraînement.
     */
    private fun loadLabels() {
        try {
            labels = context.assets.open(LABELS_FILENAME)
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
            Log.d(TAG, "✅ ${labels.size} labels chargés")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Impossible de charger labels.txt : ${e.message}")
            labels = emptyList()
        }
    }

    /**
     * Charge le modèle TFLite depuis assets/.
     * Détecte automatiquement si le modèle est FLOAT32 ou INT8.
     * Si le fichier n'existe pas, reste en mode démo.
     */
    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile()
            val interp = Interpreter(modelBuffer)

            // Détecter le type d'entrée (FLOAT32 pour un modèle classique, UINT8 pour INT8)
            inputDataType = interp.getInputTensor(0).dataType()
            outputDataType = interp.getOutputTensor(0).dataType()
            outputClassCount = interp.getOutputTensor(0).shape().last()

            interpreter = interp

            Log.d(TAG, "✅ Modèle TFLite chargé — entrée: $inputDataType, sortie: $outputDataType, classes: $outputClassCount")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Modèle TFLite non trouvé → Mode DÉMO activé (${e.message})")
            interpreter = null
        }
    }

    /**
     * Lit le fichier .tflite depuis assets/ et le mappe en mémoire.
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILENAME)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    // ==========================================
    // Classification
    // ==========================================

    /**
     * Résultat d'une classification IA.
     *
     * @param label Nom de la maladie détectée (ex: "Tomato___Late_blight")
     * @param confidence Score de confiance entre 0.0 et 1.0
     * @param isDemo True si le résultat provient du mode démo
     */
    data class ClassificationResult(
        val label: String,
        val confidence: Float,
        val isDemo: Boolean = false
    )

    /**
     * Classifie un Bitmap et retourne le résultat.
     *
     * Si le modèle TFLite est chargé → vraie inférence IA.
     * Sinon → résultat démo simulé pour le développement.
     *
     * @param bitmap Image capturée par la caméra
     * @return Résultat de classification (label + confiance)
     */
    fun classify(bitmap: Bitmap): ClassificationResult {
        return if (interpreter != null) {
            classifyWithModel(bitmap)
        } else {
            classifyDemo()
        }
    }

    /**
     * Classification réelle avec le modèle TFLite.
     *
     * Supporte automatiquement les modèles FLOAT32 et INT8 quantifiés.
     *
     * Étapes :
     * 1. Redimensionner l'image à 224×224
     * 2. Convertir en ByteBuffer selon le type d'entrée du modèle
     * 3. Exécuter l'inférence
     * 4. Lire les probabilités selon le type de sortie
     * 5. Trouver le label avec la confiance maximale
     */
    private fun classifyWithModel(bitmap: Bitmap): ClassificationResult {
        val numClasses = outputClassCount.coerceAtLeast(labels.size)

        // 1. Redimensionner l'image
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        // 2. Convertir en ByteBuffer adapté au type d'entrée du modèle
        val inputBuffer = when (inputDataType) {
            DataType.UINT8 -> bitmapToByteBufferUint8(resizedBitmap)
            else -> bitmapToByteBufferFloat32(resizedBitmap)
        }

        // 3-4. Exécuter l'inférence et lire les probabilités selon le type de sortie
        val probabilities = when (outputDataType) {
            DataType.UINT8 -> {
                val outputBuffer = ByteBuffer.allocateDirect(numClasses)
                outputBuffer.order(ByteOrder.nativeOrder())
                interpreter?.run(inputBuffer, outputBuffer)
                outputBuffer.rewind()
                // Dé-quantifier UINT8 → FLOAT (0-255 → 0.0-1.0)
                FloatArray(numClasses) { (outputBuffer.get().toInt() and 0xFF) / 255.0f }
            }
            else -> {
                val outputArray = Array(1) { FloatArray(numClasses) }
                interpreter?.run(inputBuffer, outputArray)
                outputArray[0]
            }
        }

        // 5. Trouver le meilleur résultat
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val maxConfidence = probabilities[maxIndex]
        val bestLabel = if (maxIndex < labels.size) labels[maxIndex] else "Unknown"

        Log.d(TAG, "🔬 Résultat IA : $bestLabel (${"%.1f".format(maxConfidence * 100)}%)")

        return ClassificationResult(
            label = bestLabel,
            confidence = maxConfidence,
            isDemo = false
        )
    }

    /**
     * Convertit un Bitmap en ByteBuffer FLOAT32 avec pixels bruts (0.0 - 255.0).
     *
     * IMPORTANT : Le modèle MobileNetV2 contient une couche Rescaling(1/127.5, -1)
     * intégrée qui convertit [0, 255] → [-1, 1] en interne.
     * Il ne faut donc PAS normaliser ici — envoyer les pixels bruts en float32.
     *
     * Taille : 1 × 224 × 224 × 3 × 4 bytes = 602 112 bytes
     */
    private fun bitmapToByteBufferFloat32(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            byteBuffer.putFloat((pixel shr 16 and 0xFF).toFloat())
            byteBuffer.putFloat((pixel shr 8 and 0xFF).toFloat())
            byteBuffer.putFloat((pixel and 0xFF).toFloat())
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * Convertit un Bitmap en ByteBuffer UINT8 (0 - 255).
     * Utilisé pour les modèles INT8 quantifiés.
     * Taille : 1 × 224 × 224 × 3 × 1 byte = 150 528 bytes
     */
    private fun bitmapToByteBufferUint8(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * CHANNELS)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            byteBuffer.put((pixel shr 16 and 0xFF).toByte())
            byteBuffer.put((pixel shr 8 and 0xFF).toByte())
            byteBuffer.put((pixel and 0xFF).toByte())
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * Mode DÉMO : génère un résultat simulé aléatoire.
     *
     * Utilisé quand le modèle .tflite n'est pas encore disponible.
     * Permet de tester toute la chaîne UI sans le modèle.
     */
    private fun classifyDemo(): ClassificationResult {
        val randomLabel = labels.randomOrNull() ?: "Healthy"
        val randomConfidence = (70..98).random() / 100f

        Log.d(TAG, "🎭 Mode DÉMO : $randomLabel (${"%.1f".format(randomConfidence * 100)}%)")

        return ClassificationResult(
            label = randomLabel,
            confidence = randomConfidence,
            isDemo = true
        )
    }

    /**
     * Libère les ressources du modèle TFLite.
     * Appeler quand l'application se ferme.
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "🔒 Classifieur TFLite fermé")
    }
}

