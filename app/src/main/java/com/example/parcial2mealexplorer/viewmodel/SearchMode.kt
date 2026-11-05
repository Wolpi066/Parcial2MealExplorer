package com.example.parcial2mealexplorer.viewmodel

/**
 * Por que campo se busca. Cada uno va a un endpoint distinto de la API:
 * NAME a search.php?s= y INGREDIENT a filter.php?i=
 */
enum class SearchMode {
    NAME,
    INGREDIENT
}
