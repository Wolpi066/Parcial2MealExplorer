package com.example.parcial2mealexplorer.service

import com.example.parcial2mealexplorer.model.CategoryResponse
import com.example.parcial2mealexplorer.model.MealDetailResponse
import com.example.parcial2mealexplorer.model.MealResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoints de TheMealDB que usa la app.
 *
 * Retrofit toma esta interfaz y genera solo la implementacion que arma la URL
 * y hace la llamada HTTP. Todas son suspend: se pueden pausar mientras esperan
 * la respuesta sin bloquear el hilo en el que corren.
 *
 * Devuelven Response<...> y no el objeto directo para poder mirar el codigo
 * HTTP (isSuccessful) antes de leer el cuerpo.
 */
interface MealApiService {

    /** Las 14 categorias, para los chips de la pantalla principal. */
    @GET("categories.php")
    suspend fun getCategories(): Response<CategoryResponse>

    /** Recetas de una categoria. Solo trae id, nombre y foto de cada una. */
    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): Response<MealResponse>

    /** Busca recetas cuyo nombre contenga el texto. */
    @GET("search.php")
    suspend fun searchMealsByName(@Query("s") name: String): Response<MealResponse>

    /** Recetas que llevan ese ingrediente. La API solo entiende ingredientes en ingles. */
    @GET("filter.php")
    suspend fun searchMealsByIngredient(@Query("i") ingredient: String): Response<MealResponse>

    /** Una receta completa a partir de su id. */
    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): Response<MealDetailResponse>

    /** Una receta completa elegida al azar por la API. */
    @GET("random.php")
    suspend fun getRandomMeal(): Response<MealDetailResponse>
}
