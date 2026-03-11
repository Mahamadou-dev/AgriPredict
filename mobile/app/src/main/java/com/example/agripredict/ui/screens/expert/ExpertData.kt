package com.example.agripredict.ui.screens.expert

/**
 * Données des experts agricoles pour l'annuaire.
 * Liste statique adaptée au contexte Niger/Afrique de l'Ouest.
 */
data class ExpertContact(
    val name: String,
    val specialty: String,
    val phone: String,
    val zone: String,
    val initials: String = name.split(" ").take(2).joinToString("") { it.first().uppercase() }
)

val expertContacts = listOf(
    ExpertContact(
        name = "Dr. Ibrahim Moussa",
        specialty = "Phytopathologie — Manioc & Maïs",
        phone = "+22790123456",
        zone = "Niamey"
    ),
    ExpertContact(
        name = "Mme Aïssa Abdou",
        specialty = "Agronomie — Cultures maraîchères",
        phone = "+22796543210",
        zone = "Maradi"
    ),
    ExpertContact(
        name = "M. Oumarou Garba",
        specialty = "Protection des végétaux",
        phone = "+22794567890",
        zone = "Zinder"
    ),
    ExpertContact(
        name = "Dr. Fatima Hamidou",
        specialty = "Entomologie — Lutte biologique",
        phone = "+22791234567",
        zone = "Tahoua"
    ),
    ExpertContact(
        name = "M. Adamou Souleymane",
        specialty = "Conseil agricole — INRAN",
        phone = "+22797654321",
        zone = "Dosso"
    )
)

