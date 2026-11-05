package com.example.parcial2mealexplorer.model

import com.google.gson.annotations.SerializedName

/**
 * Receta completa tal como viene de la API (lookup.php y random.php).
 *
 * Es solo para leer el JSON: apenas llega se convierte en Recipe con toRecipe(),
 * que es la clase que usa el resto de la app.
 *
 * La API no manda los ingredientes en una lista sino en 20 campos sueltos
 * (strIngredient1 a strIngredient20) y las medidas en otros 20 (strMeasure1 a
 * strMeasure20). Las recetas con menos ingredientes los mandan vacios o null.
 * Estos 40 campos se llaman igual que en el JSON, por eso no llevan
 * @SerializedName.
 */
data class MealDetail(

    @SerializedName("idMeal")
    val id: String,

    @SerializedName("strMeal")
    val name: String,

    @SerializedName("strCategory")
    val category: String?,

    // Pais o cocina de origen: Italian, Mexican, Japanese...
    @SerializedName("strArea")
    val area: String?,

    @SerializedName("strInstructions")
    val instructions: String?,

    @SerializedName("strMealThumb")
    val thumbnail: String?,

    @SerializedName("strYoutube")
    val youtubeUrl: String?,

    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,

    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?,
    val strMeasure16: String?,
    val strMeasure17: String?,
    val strMeasure18: String?,
    val strMeasure19: String?,
    val strMeasure20: String?
) {

    /**
     * Junta los 20 ingredientes con su medida en una sola lista, salteando los
     * que vienen vacios. Cada elemento queda como "Chicken: 1 lb".
     */
    fun getIngredients(): List<String> {
        val names = listOf(
            strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
            strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
            strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
            strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20
        )
        val measures = listOf(
            strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
            strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
            strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
            strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20
        )

        val ingredients = mutableListOf<String>()

        // El ingrediente 1 va con la medida 1, el 2 con la 2, y asi.
        for (i in names.indices) {
            val name = names[i]?.trim()
            val measure = measures[i]?.trim()

            // Un ingrediente vacio significa que ese lugar no se usa.
            if (name.isNullOrEmpty()) continue

            if (measure.isNullOrEmpty()) {
                ingredients.add(name)
            } else {
                ingredients.add("$name: $measure")
            }
        }
        return ingredients
    }

    /**
     * Pasa la receta de la API al modelo de la app. Los ingredientes se juntan
     * una sola vez aca, y los campos que pueden venir null quedan como texto
     * vacio para no andar chequeando null en las pantallas.
     */
    fun toRecipe(): Recipe {
        return Recipe(
            id = id,
            name = name,
            category = category ?: "",
            area = area ?: "",
            thumbnail = thumbnail ?: "",
            instructions = instructions ?: "",
            ingredients = getIngredients(),
            youtubeUrl = youtubeUrl ?: ""
        )
    }
}
