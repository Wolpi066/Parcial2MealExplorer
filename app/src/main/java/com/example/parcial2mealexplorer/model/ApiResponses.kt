package com.example.parcial2mealexplorer.model

import com.google.gson.annotations.SerializedName

// TheMealDB nunca devuelve una lista suelta: siempre la mete dentro de un objeto,
// por ejemplo {"meals": [ ... ]}. Estas clases representan ese objeto de afuera.
//
// Ojo: cuando no hay resultados la API no manda una lista vacia sino
// {"meals": null}. Por eso las listas son nulables.

/** Respuesta de categories.php. */
data class CategoryResponse(

    @SerializedName("categories")
    val categories: List<Category>?
)

/**
 * Respuesta de filter.php y de search.php.
 *
 * search.php manda la receta completa, pero para la grilla alcanza con id,
 * nombre y foto: Gson completa esos tres campos de Meal e ignora el resto.
 */
data class MealResponse(

    @SerializedName("meals")
    val meals: List<Meal>?
)

/** Respuesta de lookup.php y random.php: una lista con una sola receta. */
data class MealDetailResponse(

    @SerializedName("meals")
    val meals: List<MealDetail>?
)
