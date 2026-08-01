package io.github.zorinontop.twojastokrotka.models

enum class SortOption(val label: String) {
    DOMYSLNE("DOMYŚLNE"),
    PO_KATEGORII("PO KATEGORII"),
    OD_NAJNIZSZEJ_CENY("OD NAJNIŻSZEJ CENY"),
    OD_NAJWYZSZEJ_CENY("OD NAJWYŻSZEJ CENY"),
    OD_NAJNIZSZEJ_ILOSCI_PLATKOW("OD NAJNIŻSZEJ ILOŚCI PŁATKÓW ZA KUPON"),
    OD_NAJWYZSZEJ_ILOSCI_PLATKOW("OD NAJWYŻSZEJ ILOŚCI PŁATKÓW ZA KUPON")
}

data class FilterCriteria(
    val selectedPetals: Set<Int> = emptySet(),
    val selectedCategories: Set<String> = emptySet()
)
