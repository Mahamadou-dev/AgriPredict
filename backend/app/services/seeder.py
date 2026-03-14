"""
Script de seed pour pré-charger la base de connaissances.

Ce script insère les 24 maladies et 41 traitements correspondant
au modèle IA déployé sur le mobile.

Les IDs sont fixes et correspondent aux classes du modèle TFLite.
"""

from sqlalchemy.orm import Session

from app.models.entities import Alert, Maladie, ModeleIA, Traitement


def seed_database(db: Session) -> dict:
    """
    Pré-charge la base de données avec les données initiales.
    
    Returns:
        Dictionnaire avec le nombre d'éléments créés
    """
    stats = {
        "maladies": 0,
        "traitements": 0,
        "alertes": 0,
        "modeles": 0,
    }
    
    # Vérifier si déjà seedé
    if db.query(Maladie).count() > 0:
        return {"message": "Base déjà initialisée", **stats}
    
    # ========================================================================
    # MALADIES (24 classes du modèle IA)
    # ========================================================================
    
    maladies_data = [
        # Manioc (Cassava) - 5 classes
        (1, "Brûlure bactérienne du manioc (CBB)", "Xanthomonas axonopodis pv. manihotis",
         "Maladie bactérienne causant des taches angulaires sur les feuilles, un flétrissement et une exsudation de gomme sur les tiges."),
        (2, "Maladie des stries brunes du manioc (CBSD)", "Virus de la strie brune du manioc",
         "Maladie virale provoquant des stries jaunes/brunes sur les feuilles et une nécrose des racines."),
        (3, "Mosaïque verte du manioc (CGM)", "Virus de la mosaïque africaine du manioc",
         "Maladie virale causant une mosaïque verte et jaune sur les feuilles avec déformation."),
        (4, "Manioc sain", "", "Plante de manioc sans symptômes de maladie."),
        (5, "Mosaïque du manioc (CMD)", "Virus de la mosaïque du manioc",
         "Maladie virale majeure causant une mosaïque chlorotique et une réduction de la taille des feuilles."),
        
        # Maïs (Corn) - 4 classes
        (6, "Cercosporiose du maïs", "Cercospora zeae-maydis",
         "Maladie fongique causant des lésions rectangulaires gris-brun entre les nervures."),
        (7, "Rouille commune du maïs", "Puccinia sorghi",
         "Maladie fongique produisant des pustules rouge-brun sur les feuilles."),
        (8, "Maïs sain", "", "Plant de maïs sans symptômes de maladie."),
        (9, "Brûlure du Nord du maïs", "Exserohilum turcicum",
         "Maladie fongique causant de grandes lésions elliptiques gris-vert."),
        
        # Poivron (Pepper) - 2 classes
        (10, "Tache bactérienne du poivron", "Xanthomonas campestris pv. vesicatoria",
         "Maladie bactérienne causant des taches aqueuses devenant brunes sur les feuilles."),
        (11, "Poivron sain", "", "Plant de poivron sans symptômes de maladie."),
        
        # Pomme de terre (Potato) - 3 classes
        (12, "Alternariose de la pomme de terre", "Alternaria solani",
         "Maladie fongique causant des lésions concentriques brunes en forme de cible."),
        (13, "Pomme de terre saine", "", "Plant de pomme de terre sans symptômes de maladie."),
        (14, "Mildiou de la pomme de terre", "Phytophthora infestans",
         "Maladie fongique dévastatrice causant des lésions aqueuses et un flétrissement rapide."),
        
        # Tomate (Tomato) - 10 classes
        (15, "Tache bactérienne de la tomate", "Xanthomonas campestris pv. vesicatoria",
         "Maladie bactérienne causant des taches aqueuses sur les feuilles et fruits."),
        (16, "Alternariose de la tomate", "Alternaria solani",
         "Maladie fongique causant des taches brunes concentriques sur les feuilles basses."),
        (17, "Tomate saine", "", "Plant de tomate sans symptômes de maladie."),
        (18, "Mildiou de la tomate", "Phytophthora infestans",
         "Maladie fongique causant des lésions aqueuses et un brunissement rapide."),
        (19, "Moisissure des feuilles de tomate", "Passalora fulva",
         "Maladie fongique causant des taches jaunes sur le dessus et moisissure olive dessous."),
        (20, "Septoriose de la tomate", "Septoria lycopersici",
         "Maladie fongique causant de petites taches circulaires à centre gris."),
        (21, "Acariens de la tomate", "Tetranychus urticae",
         "Dégâts causés par les acariens rouges, jaunissement et bronzage des feuilles."),
        (22, "Tache cible de la tomate", "Corynespora cassiicola",
         "Maladie fongique causant des lésions concentriques avec halo jaune."),
        (23, "Virus de la mosaïque de la tomate", "Tomato mosaic virus",
         "Maladie virale causant une mosaïque, déformation et réduction des fruits."),
        (24, "Virus de l'enroulement jaune de la tomate", "Tomato yellow leaf curl virus",
         "Maladie virale causant un enroulement et jaunissement des feuilles, rabougrissement."),
    ]
    
    for id, nom, nom_sci, desc in maladies_data:
        maladie = Maladie(
            id=id,
            nom_commun=nom,
            nom_scientifique=nom_sci,
            description=desc,
        )
        db.merge(maladie)
        stats["maladies"] += 1
    
    db.flush()
    
    # ========================================================================
    # TRAITEMENTS
    # ========================================================================
    
    traitements_data = [
        # CBB (1)
        (1, "Utiliser des boutures saines", "Sélectionner des boutures provenant de plantes certifiées indemnes.", "N/A", 1),
        (2, "Éliminer les plants infectés", "Arracher et brûler les plants malades pour limiter la propagation.", "Immédiat", 1),
        
        # CBSD (2)
        (3, "Variétés résistantes", "Planter des variétés de manioc résistantes au CBSD.", "N/A", 2),
        (4, "Récolte précoce", "Récolter les racines avant qu'elles ne soient trop endommagées.", "4-6 mois", 2),
        
        # CGM (3)
        (5, "Contrôle des vecteurs", "Éliminer les mouches blanches qui transmettent le virus.", "Régulier", 3),
        
        # Manioc sain (4)
        (6, "Entretien préventif", "Maintenir une bonne hygiène du champ et surveiller régulièrement.", "Hebdomadaire", 4),
        
        # CMD (5)
        (7, "Variétés tolérantes", "Utiliser des variétés tolérantes à la mosaïque.", "N/A", 5),
        (8, "Élimination des repousses", "Détruire les repousses qui peuvent héberger le virus.", "Après récolte", 5),
        
        # Cercosporiose maïs (6)
        (9, "Rotation des cultures", "Alterner le maïs avec d'autres cultures non hôtes.", "Annuelle", 6),
        (10, "Fongicide foliaire", "Appliquer un fongicide à base de strobilurine.", "200-300 ml/ha", 6),
        
        # Rouille maïs (7)
        (11, "Variétés résistantes", "Choisir des hybrides avec résistance à la rouille.", "N/A", 7),
        (12, "Fongicide préventif", "Appliquer un fongicide triazole en début d'infection.", "125-250 ml/ha", 7),
        
        # Maïs sain (8)
        (13, "Fertilisation équilibrée", "Apporter azote, phosphore et potassium selon les besoins.", "Selon analyse sol", 8),
        
        # Brûlure Nord maïs (9)
        (14, "Hybrides résistants", "Planter des hybrides avec gènes de résistance Ht.", "N/A", 9),
        (15, "Destruction des résidus", "Enfouir ou brûler les résidus de culture après récolte.", "Post-récolte", 9),
        
        # Tache bactérienne poivron (10)
        (16, "Cuivre préventif", "Pulvériser de l'hydroxyde de cuivre en prévention.", "2-3 kg/ha", 10),
        (17, "Éviter l'irrigation par aspersion", "Préférer le goutte-à-goutte pour limiter l'humidité foliaire.", "N/A", 10),
        
        # Poivron sain (11)
        (18, "Paillage", "Pailler le sol pour maintenir l'humidité et limiter les éclaboussures.", "5-10 cm", 11),
        
        # Alternariose pomme de terre (12)
        (19, "Fongicide contact", "Appliquer du mancozèbe en prévention.", "2-2.5 kg/ha", 12),
        (20, "Fongicide systémique", "Utiliser du chlorothalonil en alternance.", "1.5-2 L/ha", 12),
        
        # Pomme de terre saine (13)
        (21, "Buttage régulier", "Butter les plants pour protéger les tubercules.", "2-3 fois", 13),
        
        # Mildiou pomme de terre (14)
        (22, "Fongicide préventif", "Appliquer du métalaxyl + mancozèbe dès conditions favorables.", "2.5 kg/ha", 14),
        (23, "Destruction du feuillage", "Détruire le feuillage 2 semaines avant récolte si mildiou présent.", "Avant récolte", 14),
        
        # Tache bactérienne tomate (15)
        (24, "Cuivre", "Pulvériser du sulfate de cuivre.", "3-4 kg/ha", 15),
        (25, "Semences certifiées", "Utiliser uniquement des semences certifiées saines.", "N/A", 15),
        
        # Alternariose tomate (16)
        (26, "Fongicide foliaire", "Appliquer du chlorothalonil ou mancozèbe.", "2 kg/ha", 16),
        (27, "Éliminer feuilles basses", "Supprimer les feuilles touchées en bas du plant.", "Dès symptômes", 16),
        
        # Tomate saine (17)
        (28, "Tuteurage", "Tuteurer les plants pour améliorer l'aération.", "Dès plantation", 17),
        (29, "Arrosage au pied", "Arroser au pied, jamais sur le feuillage.", "Quotidien", 17),
        
        # Mildiou tomate (18)
        (30, "Fongicide systémique", "Appliquer du métalaxyl en curatif.", "2.5 L/ha", 18),
        (31, "Aération de la serre", "Bien ventiler pour réduire l'humidité.", "Permanent", 18),
        
        # Moisissure feuilles tomate (19)
        (32, "Réduire l'humidité", "Maintenir une humidité relative inférieure à 85%.", "<85%", 19),
        (33, "Fongicide spécifique", "Appliquer un fongicide à base de difénoconazole.", "0.5 L/ha", 19),
        
        # Septoriose tomate (20)
        (34, "Éliminer débris", "Nettoyer les débris végétaux autour des plants.", "Régulier", 20),
        (35, "Fongicide préventif", "Pulvériser du mancozèbe en prévention.", "2 kg/ha", 20),
        
        # Acariens tomate (21)
        (36, "Acaricide", "Appliquer un acaricide comme l'abamectine.", "0.5 L/ha", 21),
        (37, "Lutte biologique", "Introduire des phytoséiides prédateurs.", "Selon pression", 21),
        
        # Tache cible tomate (22)
        (38, "Rotation longue", "Attendre 3-4 ans avant de replanter des solanacées.", "3-4 ans", 22),
        (39, "Fongicide", "Appliquer un fongicide à base de boscalid.", "0.5 kg/ha", 22),
        
        # Mosaïque tomate (23)
        (40, "Hygiène stricte", "Désinfecter les outils entre chaque plant manipulé.", "Permanent", 23),
        
        # TYLCV (24)
        (41, "Contrôle mouche blanche", "Lutter contre Bemisia tabaci avec insecticides ou filets.", "Régulier", 24),
    ]
    
    for id, titre, desc, dosage, maladie_id in traitements_data:
        traitement = Traitement(
            id=id,
            titre=titre,
            description=desc,
            dosage=dosage,
            maladie_id=maladie_id,
        )
        db.merge(traitement)
        stats["traitements"] += 1
    
    db.flush()
    
    # ========================================================================
    # MODÈLE IA
    # ========================================================================
    
    model = ModeleIA(
        version="1.0.0",
        framework="tflite",
        precision=95.0,
        input_size=224,
    )
    db.merge(model)
    stats["modeles"] += 1
    
    db.commit()
    
    return stats


def seed_sample_alerts(db: Session) -> int:
    """
    Ajoute des alertes d'exemple pour le Niger.
    """
    if db.query(Alert).count() > 0:
        return 0
    
    from datetime import datetime, timedelta, timezone
    
    alerts_data = [
        ("⚠️ Alerte Mildiou - Région de Niamey", "Niamey", 0.8, 14),
        ("🌧️ Fortes pluies prévues cette semaine", "Zinder", 0.5, None),
        ("🦗 Présence de criquets signalée", "Maradi", 0.7, None),
        ("🌡️ Canicule - Protégez vos cultures", "Tahoua", 0.6, None),
        ("📢 Campagne de vaccination du bétail", "Dosso", 0.3, None),
        ("🌱 Distribution de semences améliorées", "Tillabéri", 0.2, None),
    ]
    
    count = 0
    for message, zone, gravite, maladie_id in alerts_data:
        alert = Alert(
            message=message,
            zone=zone,
            gravite=gravite,
            maladie_id=maladie_id,
            date_expiration=datetime.now(timezone.utc) + timedelta(days=30),
        )
        db.add(alert)
        count += 1
    
    db.commit()
    return count

