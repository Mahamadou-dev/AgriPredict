package com.example.agripredict.data.local

import com.example.agripredict.data.local.dao.AlerteDao
import com.example.agripredict.data.local.dao.MaladieDao
import com.example.agripredict.data.local.dao.TraitementDao
import com.example.agripredict.data.local.entity.AlerteEntity
import com.example.agripredict.data.local.entity.MaladieEntity
import com.example.agripredict.data.local.entity.TraitementEntity
import com.example.agripredict.sync.SyncStatus
import java.util.UUID

/**
 * Données initiales pré-chargées pour la base de connaissances.
 *
 * Contient les 24 classes détectées par le modèle IA (MobileNetV2),
 * avec des descriptions réalistes et des traitements détaillés
 * adaptés au contexte ouest-africain (Niger, Nigeria, Cameroun).
 *
 * Ces données seront remplacées par la synchronisation serveur
 * une fois le backend opérationnel.
 */
object DatabaseSeeder {

    suspend fun seedIfEmpty(
        maladieDao: MaladieDao,
        traitementDao: TraitementDao,
        alerteDao: AlerteDao
    ) {
        val existing = maladieDao.getAll()
        if (existing.isNotEmpty()) return

        maladieDao.insertAll(maladies)
        traitementDao.insertAll(traitements)

        // Insérer les alertes fictives
        alertes.forEach { alerteDao.insert(it) }
    }

    // ═══════════════════════════════════════════
    // MALADIES (24 classes du modèle)
    // ═══════════════════════════════════════════

    private val maladies = listOf(
        // === MANIOC (Cassava) — 5 classes ===
        MaladieEntity(
            id = 1,
            nomCommun = "Brûlure bactérienne du manioc (CBB)",
            nomScientifique = "Xanthomonas axonopodis pv. manihotis",
            description = "Maladie bactérienne grave qui provoque des lésions angulaires sur les feuilles, un flétrissement des tiges et un exsudat bactérien. Très répandue en Afrique de l'Ouest, elle peut réduire les rendements de 50 à 100%. La transmission se fait par boutures infectées et pluies battantes."
        ),
        MaladieEntity(
            id = 2,
            nomCommun = "Maladie des stries brunes du manioc (CBSD)",
            nomScientifique = "Cassava brown streak virus (CBSV)",
            description = "Virose qui cause des stries jaune-brun sur les tiges et une nécrose de la racine tubéreuse. Les tubercules deviennent impropres à la consommation. Transmise par la mouche blanche Bemisia tabaci. En progression en Afrique de l'Ouest."
        ),
        MaladieEntity(
            id = 3,
            nomCommun = "Marbrure verte du manioc (CGM)",
            nomScientifique = "Cassava green mottle virus",
            description = "Virus provoquant une marbrure vert pâle à vert foncé sur les feuilles. Affecte la photosynthèse et réduit la croissance. Transmission par la mouche blanche et les boutures infectées. Moins sévère que la mosaïque mais réduit les rendements."
        ),
        MaladieEntity(
            id = 4,
            nomCommun = "Manioc sain",
            nomScientifique = "",
            description = "Plant de manioc en bonne santé, sans symptômes de maladie. Feuilles vertes uniformes, tiges robustes. Continuer les bonnes pratiques culturales : rotation, drainage, variétés résistantes."
        ),
        MaladieEntity(
            id = 5,
            nomCommun = "Mosaïque du manioc (CMD)",
            nomScientifique = "African cassava mosaic virus (ACMV)",
            description = "Virose la plus dévastatrice du manioc en Afrique. Provoque une mosaïque jaune-verte sur les feuilles, une déformation foliaire et un rabougrissement. Transmise par la mouche blanche Bemisia tabaci. Peut entraîner 95% de perte de rendement."
        ),

        // === MAÏS (Corn) — 4 classes ===
        MaladieEntity(
            id = 6,
            nomCommun = "Cercosporiose du maïs",
            nomScientifique = "Cercospora zeae-maydis",
            description = "Champignon provoquant des taches rectangulaires gris clair entre les nervures des feuilles. Se développe en conditions chaudes et humides. Réduit la photosynthèse et peut entraîner des pertes de 30 à 50% dans les cas graves."
        ),
        MaladieEntity(
            id = 7,
            nomCommun = "Rouille commune du maïs",
            nomScientifique = "Puccinia sorghi",
            description = "Champignon formant des pustules rouille-brun à orange sur les deux faces des feuilles. Très fréquent en saison des pluies. Réduit la surface foliaire active. Pertes de rendement de 10 à 40% selon la sévérité."
        ),
        MaladieEntity(
            id = 8,
            nomCommun = "Maïs sain",
            nomScientifique = "",
            description = "Plant de maïs en bonne santé avec des feuilles vertes et étalées, des tiges vigoureuses. Maintenir les bonnes pratiques : densité de semis adaptée, fertilisation équilibrée, désherbage régulier."
        ),
        MaladieEntity(
            id = 9,
            nomCommun = "Helminthosporiose du maïs",
            nomScientifique = "Exserohilum turcicum",
            description = "Champignon causant de grandes lésions elliptiques gris-vert à brun sur les feuilles. Commence par les feuilles basses et progresse vers le haut. Favorisé par l'humidité. Peut réduire les rendements de 30 à 70%."
        ),

        // === POIVRON (Pepper) — 2 classes ===
        MaladieEntity(
            id = 10,
            nomCommun = "Tache bactérienne du poivron",
            nomScientifique = "Xanthomonas campestris pv. vesicatoria",
            description = "Bactérie provoquant des taches aqueuses puis brunes sur les feuilles et fruits du poivron. Favorisée par la chaleur et l'humidité. Transmise par les éclaboussures de pluie. Peut détruire jusqu'à 60% de la récolte."
        ),
        MaladieEntity(
            id = 11,
            nomCommun = "Poivron sain",
            nomScientifique = "",
            description = "Plant de poivron en bonne santé avec feuillage vert brillant et fruits bien formés. Maintenir un espacement adéquat, un arrosage au pied (éviter le feuillage), et une bonne rotation culturale."
        ),

        // === POMME DE TERRE (Potato) — 3 classes ===
        MaladieEntity(
            id = 12,
            nomCommun = "Alternariose de la pomme de terre",
            nomScientifique = "Alternaria solani",
            description = "Champignon causant des taches brunes concentriques (cible) sur les feuilles basses. Progresse vers le haut en conditions chaudes et sèches. Réduit la surface foliaire et le calibre des tubercules. Pertes de 20 à 50%."
        ),
        MaladieEntity(
            id = 13,
            nomCommun = "Pomme de terre saine",
            nomScientifique = "",
            description = "Plant de pomme de terre vigoureux avec feuillage vert dense. Bonne production de tubercules. Maintenir le buttage, l'irrigation régulière et surveiller les ravageurs (doryphore, pucerons)."
        ),
        MaladieEntity(
            id = 14,
            nomCommun = "Mildiou de la pomme de terre",
            nomScientifique = "Phytophthora infestans",
            description = "Maladie fongique dévastatrice causant des taches brunes huileuses sur les feuilles avec un duvet blanc au revers. Progression très rapide par temps humide. Le pathogène responsable de la Grande Famine d'Irlande. Pertes totales possibles."
        ),

        // === TOMATE (Tomato) — 10 classes ===
        MaladieEntity(
            id = 15,
            nomCommun = "Tache bactérienne de la tomate",
            nomScientifique = "Xanthomonas vesicatoria",
            description = "Bactérie causant de petites taches sombres aqueuses sur feuilles, tiges et fruits. Les fruits présentent des lésions liégeuses surélevées. Très fréquente en climat tropical humide. Transmise par les semences et les éclaboussures."
        ),
        MaladieEntity(
            id = 16,
            nomCommun = "Alternariose de la tomate",
            nomScientifique = "Alternaria solani",
            description = "Champignon formant des taches brunes à anneaux concentriques (œil de bœuf) sur les feuilles basses. Progresse de bas en haut. Favorisée par l'humidité et les températures moyennes (24-29°C). Pertes de 35 à 78%."
        ),
        MaladieEntity(
            id = 17,
            nomCommun = "Tomate saine",
            nomScientifique = "",
            description = "Plant de tomate vigoureux avec feuilles vertes, fleurs bien formées et fruits mûrissant normalement. Maintenir le tuteurage, l'effeuillage des gourmands, et l'irrigation régulière au pied."
        ),
        MaladieEntity(
            id = 18,
            nomCommun = "Mildiou de la tomate",
            nomScientifique = "Phytophthora infestans",
            description = "Maladie fongique grave causant des taches aqueuses brun-vert sur feuilles et tiges, avec un feutrage blanc sous les feuilles. Progression fulgurante par temps frais et humide. Peut détruire la culture en quelques jours."
        ),
        MaladieEntity(
            id = 19,
            nomCommun = "Cladosporiose de la tomate",
            nomScientifique = "Passalora fulva (syn. Cladosporium fulvum)",
            description = "Champignon formant des taches jaune pâle sur la face supérieure des feuilles, avec un feutrage brun-olive au revers. Favorisé par l'humidité élevée (>85%) et températures de 22-25°C. Fréquent sous abri."
        ),
        MaladieEntity(
            id = 20,
            nomCommun = "Septoriose de la tomate",
            nomScientifique = "Septoria lycopersici",
            description = "Champignon causant de nombreuses petites taches circulaires avec un centre gris et une bordure brune. Points noirs (pycnides) visibles au centre. Attaque les feuilles basses en premier. Pertes de 30 à 50%."
        ),
        MaladieEntity(
            id = 21,
            nomCommun = "Acarien à deux points (Tomate)",
            nomScientifique = "Tetranychus urticae",
            description = "Acarien microscopique qui pique les cellules foliaires, causant des ponctuations jaunes puis un bronzage du feuillage. Toiles fines visibles sous les feuilles. Prolifère en conditions chaudes et sèches. Peut défolier totalement le plant."
        ),
        MaladieEntity(
            id = 22,
            nomCommun = "Tache cible de la tomate",
            nomScientifique = "Corynespora cassiicola",
            description = "Champignon formant des taches circulaires brun foncé avec des anneaux concentriques sur les feuilles. Ressemble à l'alternariose mais avec des lésions plus grandes. Favorisé par chaleur et humidité alternées."
        ),
        MaladieEntity(
            id = 23,
            nomCommun = "Virus de la mosaïque de la tomate",
            nomScientifique = "Tomato mosaic virus (ToMV)",
            description = "Virus causant une mosaïque de zones vert foncé et vert clair sur les feuilles, avec déformation. Transmission mécanique (mains, outils, contact). Virus très résistant, persiste longtemps sur les surfaces. Réduction de rendement de 20 à 90%."
        ),
        MaladieEntity(
            id = 24,
            nomCommun = "Enroulement jaune de la tomate (TYLCV)",
            nomScientifique = "Tomato yellow leaf curl virus (TYLCV)",
            description = "Geminivirus transmis par la mouche blanche Bemisia tabaci. Provoque un enroulement et jaunissement des feuilles, un rabougrissement sévère et une chute des fleurs. Pas de fruit ou fruits très petits. Pertes de 80 à 100%."
        )
    )

    // ═══════════════════════════════════════════
    // TRAITEMENTS
    // ═══════════════════════════════════════════

    private val traitements = listOf(
        // --- CBB (id=1) ---
        TraitementEntity(id = 1, titre = "Boutures saines", description = "Utiliser exclusivement des boutures provenant de plants certifiés sains. Désinfecter les outils de coupe à l'alcool entre chaque plant.", dosage = "", maladieId = 1),
        TraitementEntity(id = 2, titre = "Variétés résistantes", description = "Planter des variétés tolérantes au CBB comme TMS 30572, TME 419 ou IITA recommandées pour l'Afrique de l'Ouest.", dosage = "", maladieId = 1),
        TraitementEntity(id = 3, titre = "Cuivre (prévention)", description = "Pulvérisation de bouillie bordelaise en début de saison des pluies pour limiter la propagation bactérienne.", dosage = "20 g/L, tous les 14 jours", maladieId = 1),

        // --- CBSD (id=2) ---
        TraitementEntity(id = 4, titre = "Élimination des plants infectés", description = "Arracher et brûler immédiatement les plants présentant des symptômes. Ne pas utiliser leurs boutures pour la replantation.", dosage = "", maladieId = 2),
        TraitementEntity(id = 5, titre = "Lutte contre la mouche blanche", description = "Utiliser des pièges jaunes collants et des insecticides à base d'imidaclopride pour contrôler Bemisia tabaci vecteur du virus.", dosage = "Pièges : 20/ha ; Insecticide selon notice", maladieId = 2),

        // --- CGM (id=3) ---
        TraitementEntity(id = 6, titre = "Boutures certifiées", description = "S'approvisionner auprès de pépiniéristes certifiés. Inspecter visuellement les boutures avant plantation.", dosage = "", maladieId = 3),
        TraitementEntity(id = 7, titre = "Contrôle des vecteurs", description = "Désherbage régulier autour des champs pour réduire les refuges de mouches blanches. Installation de filets anti-insectes si possible.", dosage = "", maladieId = 3),

        // --- Manioc sain (id=4) ---
        TraitementEntity(id = 8, titre = "Bonnes pratiques culturales", description = "Rotation avec légumineuses, espacement de 1m×1m, paillage organique, fertilisation au compost. Surveillance régulière du feuillage.", dosage = "Compost : 5-10 t/ha", maladieId = 4),

        // --- CMD (id=5) ---
        TraitementEntity(id = 9, titre = "Variétés résistantes CMD", description = "Adopter les variétés résistantes développées par l'IITA : TME 419, TMS 98/0581, ou les variétés locales recommandées par l'INRAN.", dosage = "", maladieId = 5),
        TraitementEntity(id = 10, titre = "Phytoassainissement", description = "Arracher systématiquement les plants présentant des symptômes de mosaïque pour réduire la source d'inoculum dans le champ.", dosage = "", maladieId = 5),

        // --- Cercosporiose maïs (id=6) ---
        TraitementEntity(id = 11, titre = "Fongicide à base de strobilurine", description = "Application d'azoxystrobine ou de pyraclostrobine dès l'apparition des premières taches pour stopper la progression.", dosage = "Azoxystrobine 250 g/L : 1 L/ha", maladieId = 6),
        TraitementEntity(id = 12, titre = "Rotation culturale", description = "Alterner le maïs avec des cultures non hôtes (arachide, niébé, sorgho) pour briser le cycle du champignon.", dosage = "Rotation sur 2-3 ans", maladieId = 6),

        // --- Rouille maïs (id=7) ---
        TraitementEntity(id = 13, titre = "Fongicide triazole", description = "Pulvérisation de propiconazole ou tébuconazole dès l'apparition des premières pustules. Traiter les deux faces des feuilles.", dosage = "Propiconazole 250 g/L : 0.5 L/ha", maladieId = 7),
        TraitementEntity(id = 14, titre = "Variétés tolérantes", description = "Privilégier les hybrides résistants à la rouille disponibles localement. Consulter les services agricoles régionaux.", dosage = "", maladieId = 7),

        // --- Maïs sain (id=8) ---
        TraitementEntity(id = 15, titre = "Fertilisation équilibrée", description = "Apport d'azote (urée) au semis et au montaison, phosphore et potasse au semis. Le maïs est très exigeant en azote.", dosage = "N: 120kg/ha, P2O5: 60kg/ha, K2O: 40kg/ha", maladieId = 8),

        // --- Helminthosporiose maïs (id=9) ---
        TraitementEntity(id = 16, titre = "Fongicide systémique", description = "Application de mancozèbe + métalaxyl en prévention, ou de trifloxystrobine en curatif lors d'attaques importantes.", dosage = "Mancozèbe : 2-3 kg/ha", maladieId = 9),
        TraitementEntity(id = 17, titre = "Élimination des résidus", description = "Enfouir ou brûler les résidus de maïs après récolte pour détruire les spores hivernantes du champignon.", dosage = "", maladieId = 9),

        // --- Tache bact. poivron (id=10) ---
        TraitementEntity(id = 18, titre = "Cuivre + mancozèbe", description = "Pulvérisation préventive de bouillie bordelaise ou d'hydroxyde de cuivre combinée au mancozèbe. Espacer les plants pour la circulation d'air.", dosage = "Cuivre : 2 g/L + Mancozèbe : 2.5 g/L", maladieId = 10),
        TraitementEntity(id = 19, titre = "Semences traitées", description = "Traiter les semences à l'eau chaude (50°C, 25 minutes) pour éliminer la bactérie avant semis. Utiliser des semences certifiées.", dosage = "50°C pendant 25 min", maladieId = 10),

        // --- Poivron sain (id=11) ---
        TraitementEntity(id = 20, titre = "Entretien préventif", description = "Paillage pour maintenir l'humidité, arrosage au pied, espacement 50×40 cm, tuteurage des variétés hautes.", dosage = "", maladieId = 11),

        // --- Alternariose patate (id=12) ---
        TraitementEntity(id = 21, titre = "Fongicide préventif", description = "Chlorothalonil ou mancozèbe en préventif dès la fermeture du rang. Treatments tous les 7-10 jours en conditions favorables.", dosage = "Mancozèbe 80% : 2 kg/ha", maladieId = 12),
        TraitementEntity(id = 22, titre = "Irrigation maîtrisée", description = "Éviter le stress hydrique qui favorise la maladie. Irriguer régulièrement mais sans excès. Le goutte-à-goutte est idéal.", dosage = "", maladieId = 12),

        // --- Pomme de terre saine (id=13) ---
        TraitementEntity(id = 23, titre = "Buttage et surveillance", description = "Butter les plants à 2 reprises pour protéger les tubercules. Surveiller régulièrement le feuillage.", dosage = "", maladieId = 13),

        // --- Mildiou patate (id=14) ---
        TraitementEntity(id = 24, titre = "Fongicide anti-mildiou", description = "Application urgente de métalaxyl + mancozèbe ou de cymoxanil + mancozèbe dès les premiers symptômes. En cas de forte pression, traiter tous les 5-7 jours.", dosage = "Métalaxyl-M : 2.5 kg/ha", maladieId = 14),
        TraitementEntity(id = 25, titre = "Défanage précoce", description = "Si l'attaque est sévère, couper le feuillage (défanage) 2 semaines avant récolte pour protéger les tubercules.", dosage = "", maladieId = 14),

        // --- Tache bact. tomate (id=15) ---
        TraitementEntity(id = 26, titre = "Cuivre préventif", description = "Pulvérisation régulière d'hydroxyde de cuivre dès le repiquage. Éviter les irrigations par aspersion qui dispersent la bactérie.", dosage = "Cuivre : 1.5-2 g/L, tous les 7 jours", maladieId = 15),

        // --- Alternariose tomate (id=16) ---
        TraitementEntity(id = 27, titre = "Fongicide de contact", description = "Mancozèbe ou chlorothalonil en prévention. Alterner avec des fongicides systémiques (azoxystrobine) pour éviter les résistances.", dosage = "Mancozèbe : 2.5 g/L", maladieId = 16),
        TraitementEntity(id = 28, titre = "Effeuillage sanitaire", description = "Supprimer les feuilles basses atteintes pour ralentir la progression de la maladie vers le haut du plant.", dosage = "", maladieId = 16),

        // --- Tomate saine (id=17) ---
        TraitementEntity(id = 29, titre = "Bonnes pratiques tomate", description = "Tuteurage, effeuillage des gourmands, paillage, arrosage au pied. Rotation de 3 ans avec les non-solanacées.", dosage = "", maladieId = 17),

        // --- Mildiou tomate (id=18) ---
        TraitementEntity(id = 30, titre = "Traitement anti-mildiou urgent", description = "Pulvérisation immédiate de cymoxanil + mancozèbe. Intervenir dès les premiers symptômes, ne pas attendre. Renouveler après chaque pluie.", dosage = "Cymoxanil 4% + Mancozèbe 64% : 2.5 kg/ha", maladieId = 18),

        // --- Cladosporiose tomate (id=19) ---
        TraitementEntity(id = 31, titre = "Aération et espacement", description = "Améliorer la circulation d'air entre les plants. Réduire l'humidité en aérant les serres/abris. Effeuiller le bas des plants.", dosage = "", maladieId = 19),
        TraitementEntity(id = 32, titre = "Fongicide systémique", description = "Application de difénoconazole en cas d'attaque importante. En préventif, utiliser du chlorothalonil.", dosage = "Difénoconazole : 0.5 L/ha", maladieId = 19),

        // --- Septoriose tomate (id=20) ---
        TraitementEntity(id = 33, titre = "Fongicide préventif", description = "Mancozèbe ou chlorothalonil en prévention. Traiter dès les premières taches sur les feuilles basses.", dosage = "Chlorothalonil : 2 g/L", maladieId = 20),
        TraitementEntity(id = 34, titre = "Mulch / paillage", description = "Couvrir le sol au pied des plants avec de la paille ou du plastique noir pour empêcher les éclaboussures de sol contenant les spores.", dosage = "", maladieId = 20),

        // --- Acarien tomate (id=21) ---
        TraitementEntity(id = 35, titre = "Acaricide", description = "Application d'abamectine ou de spiromésifène. Bien couvrir le dessous des feuilles. Alterner les familles chimiques.", dosage = "Abamectine 18 g/L : 0.75-1 L/ha", maladieId = 21),
        TraitementEntity(id = 36, titre = "Lutte biologique", description = "Introduction du prédateur Phytoseiulus persimilis si disponible. Maintenir une humidité ambiante élevée qui défavorise les acariens.", dosage = "", maladieId = 21),

        // --- Tache cible tomate (id=22) ---
        TraitementEntity(id = 37, titre = "Fongicide à large spectre", description = "Chlorothalonil ou mancozèbe en prévention. En curatif, utiliser l'azoxystrobine + difénoconazole.", dosage = "Azoxystrobine : 0.5 L/ha", maladieId = 22),

        // --- Virus mosaïque tomate (id=23) ---
        TraitementEntity(id = 38, titre = "Hygiène stricte", description = "Se laver les mains au lait écrémé entre chaque plant lors de la taille. Désinfecter les outils au triphosphate de sodium (10%).", dosage = "TSP 10% : tremper 10 min", maladieId = 23),
        TraitementEntity(id = 39, titre = "Élimination des plants", description = "Arracher et détruire les plants virosés dès l'apparition des symptômes. Ne pas composter, brûler à distance.", dosage = "", maladieId = 23),

        // --- TYLCV tomate (id=24) ---
        TraitementEntity(id = 40, titre = "Contrôle des mouches blanches", description = "Filets anti-insectes (maille 50 mesh), pièges jaunes collants, et insecticides systémiques (imidaclopride) sur les jeunes plants.", dosage = "Pièges : 30/ha ; Imidaclopride selon notice", maladieId = 24),
        TraitementEntity(id = 41, titre = "Variétés résistantes TYLCV", description = "Utiliser des variétés portant le gène Ty-1 ou Ty-3 de résistance au TYLCV. Consulter les semenciers locaux.", dosage = "", maladieId = 24)
    )

    // ═══════════════════════════════════════════
    // ALERTES FICTIVES (réalistes)
    // ═══════════════════════════════════════════

    private val now = System.currentTimeMillis()
    private val oneDay = 86_400_000L

    private val alertes = listOf(
        AlerteEntity(
            id = UUID.randomUUID().toString(),
            message = "Risque élevé de mildiou sur tomate dans la région de Niamey. Humidité supérieure à 90% prévue cette semaine. Traiter préventivement au mancozèbe.",
            zone = "Niamey",
            gravite = 0.9f,
            dateEmission = now - (2 * oneDay),
            dateExpiration = now + (5 * oneDay),
            maladieId = 18,
            syncStatus = SyncStatus.SYNCED
        ),
        AlerteEntity(
            id = UUID.randomUUID().toString(),
            message = "Pullulation de mouches blanches signalée à Maradi. Risque accru de mosaïque du manioc (CMD). Inspecter vos parcelles et installer des pièges jaunes.",
            zone = "Maradi",
            gravite = 0.8f,
            dateEmission = now - (1 * oneDay),
            dateExpiration = now + (7 * oneDay),
            maladieId = 5,
            syncStatus = SyncStatus.SYNCED
        ),
        AlerteEntity(
            id = UUID.randomUUID().toString(),
            message = "Début de saison : pensez à utiliser des semences certifiées et des boutures saines pour éviter les maladies transmises par le matériel végétal.",
            zone = "National",
            gravite = 0.4f,
            dateEmission = now - (5 * oneDay),
            dateExpiration = now + (30 * oneDay),
            maladieId = null,
            syncStatus = SyncStatus.SYNCED
        ),
        AlerteEntity(
            id = UUID.randomUUID().toString(),
            message = "Apparition de rouille commune sur les parcelles de maïs dans la zone de Zinder. Surveillance renforcée recommandée.",
            zone = "Zinder",
            gravite = 0.6f,
            dateEmission = now,
            dateExpiration = now + (10 * oneDay),
            maladieId = 7,
            syncStatus = SyncStatus.SYNCED
        ),
        AlerteEntity(
            id = UUID.randomUUID().toString(),
            message = "Forte chaleur et sécheresse prévues à Tahoua. Risque d'infestation d'acariens sur tomate et poivron. Augmenter la fréquence d'irrigation.",
            zone = "Tahoua",
            gravite = 0.7f,
            dateEmission = now + (1 * oneDay),
            dateExpiration = now + (8 * oneDay),
            maladieId = 21,
            syncStatus = SyncStatus.SYNCED
        ),
        AlerteEntity(
            id = UUID.randomUUID().toString(),
            message = "Formation gratuite sur la lutte intégrée contre les maladies du manioc, organisée par l'INRAN à Dosso le mois prochain.",
            zone = "Dosso",
            gravite = 0.2f,
            dateEmission = now - (3 * oneDay),
            dateExpiration = now + (25 * oneDay),
            maladieId = null,
            syncStatus = SyncStatus.SYNCED
        )
    )
}
