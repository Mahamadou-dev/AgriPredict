package com.example.agripredict.data.repository

import com.example.agripredict.data.local.dao.MaladieDao
import com.example.agripredict.data.local.dao.TraitementDao
import com.example.agripredict.data.local.entity.MaladieEntity
import com.example.agripredict.domain.model.Maladie
import com.example.agripredict.domain.model.Traitement
import com.example.agripredict.domain.repository.MaladieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implémentation du repository des maladies.
 * Combine MaladieDao + TraitementDao pour fournir des objets Maladie enrichis.
 */
class MaladieRepositoryImpl(
    private val maladieDao: MaladieDao,
    private val traitementDao: TraitementDao
) : MaladieRepository {

    /**
     * Table de mapping entre les labels bruts du modèle IA (labels.txt)
     * et les IDs des maladies dans la base locale (DatabaseSeeder).
     *
     * Chaque label IA correspond exactement à un ID de maladie.
     * Les 24 classes du modèle sont mappées aux 24 maladies pré-chargées.
     */
    private val labelToMaladieId = mapOf(
        // Manioc (Cassava)
        "cassava___bacterial_blight_(cbb)" to 1,
        "cassava___brown_streak_disease_(cbsd)" to 2,
        "cassava___green_mottle_(cgm)" to 3,
        "cassava___healthy" to 4,
        "cassava___mosaic_disease_(cmd)" to 5,
        // Maïs (Corn)
        "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot" to 6,
        "Corn_(maize)___Common_rust_" to 7,
        "Corn_(maize)___healthy" to 8,
        "Corn_(maize)___Northern_Leaf_Blight" to 9,
        // Poivron (Pepper)
        "Pepper,_bell___Bacterial_spot" to 10,
        "Pepper,_bell___healthy" to 11,
        // Pomme de terre (Potato)
        "Potato___Early_blight" to 12,
        "Potato___healthy" to 13,
        "Potato___Late_blight" to 14,
        // Tomate (Tomato)
        "Tomato___Bacterial_spot" to 15,
        "Tomato___Early_blight" to 16,
        "Tomato___healthy" to 17,
        "Tomato___Late_blight" to 18,
        "Tomato___Leaf_Mold" to 19,
        "Tomato___Septoria_leaf_spot" to 20,
        "Tomato___Spider_mites Two-spotted_spider_mite" to 21,
        "Tomato___Target_Spot" to 22,
        "Tomato___Tomato_mosaic_virus" to 23,
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to 24
    )

    override fun observeAllMaladies(): Flow<List<Maladie>> {
        return maladieDao.observeAll().map { entities ->
            entities.map { entity -> enrichMaladie(entity) }
        }
    }

    override suspend fun getMaladieById(id: Int): Maladie? {
        val entity = maladieDao.getById(id) ?: return null
        return enrichMaladie(entity)
    }

    override suspend fun findByLabel(rawLabel: String): Maladie? {
        val maladieId = labelToMaladieId[rawLabel.trim()] ?: return null
        return getMaladieById(maladieId)
    }

    private suspend fun enrichMaladie(entity: MaladieEntity): Maladie {
        val traitements = traitementDao.getByMaladieId(entity.id).map { t ->
            Traitement(
                id = t.id,
                titre = t.titre,
                description = t.description,
                dosage = t.dosage,
                maladieId = t.maladieId
            )
        }
        return Maladie(
            id = entity.id,
            nomCommun = entity.nomCommun,
            nomScientifique = entity.nomScientifique,
            description = entity.description,
            traitements = traitements
        )
    }
}
