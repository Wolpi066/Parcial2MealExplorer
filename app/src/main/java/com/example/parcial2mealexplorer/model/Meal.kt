package com.example.parcial2mealexplorer.model

import com.google.gson.annotations.SerializedName

/**
 * Resumen de una receta: lo justo para mostrarla en la grilla.
 *
 * Es lo que devuelven filter.php (por categoria y por ingrediente) y lo que
 * leemos de la libreta para la lista. Para el detalle completo se usa MealDetail.
 */
data class Meal(

    @SerializedName("idMeal")
    val id: String,

    @SerializedName("strMeal")
    val name: String,

    // URL de la foto original, la mas grande.
    @SerializedName("strMealThumb")
    val thumbnail: String?
) {

    /**
     * La API da versiones mas chicas de cada foto agregando "/small" (150x150),
     * "/medium" (350x350) o "/large" (500x500) al final. En la grilla usamos la
     * mediana: pesa mucho menos que la original y no se ve pixelada.
     */
    fun getMediumThumbnail(): String? {
        if (thumbnail.isNullOrBlank()) return null
        return "$thumbnail/medium"
    }
}
