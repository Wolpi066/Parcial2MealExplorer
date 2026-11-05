package com.example.parcial2mealexplorer.repository

import android.util.Log
import com.example.parcial2mealexplorer.model.CategoryResponse
import com.example.parcial2mealexplorer.model.MealDetailResponse
import com.example.parcial2mealexplorer.model.MealResponse
import com.example.parcial2mealexplorer.service.MealApiService
import com.example.parcial2mealexplorer.service.NetworkChecker
import com.example.parcial2mealexplorer.service.RetrofitClient
import retrofit2.Response

/**
 * Puerta de entrada a los datos de TheMealDB (patron Repository).
 *
 * Los ViewModels le piden datos a esta clase y no saben que atras hay Retrofit.
 * Cada funcion deja en el Logcat que pidio y con que codigo HTTP respondio el
 * servidor (200 es OK).
 *
 * Recibe el NetworkChecker por constructor en vez de crearlo adentro, asi quien
 * arma el repositorio decide cual usar.
 */
class MealRepository(private val networkChecker: NetworkChecker) {

    // El Retrofit es el mismo para toda la app: lo da el Singleton.
    private val apiService: MealApiService = RetrofitClient.apiService

    /** true si el celular tiene una red con salida a internet. */
    fun isNetworkAvailable(): Boolean = networkChecker.isConnected()

    /** Las 14 categorias para los chips. */
    suspend fun getCategories(): Response<CategoryResponse> {
        Log.d(TAG, "Pidiendo categorias")
        val response = apiService.getCategories()
        Log.d(TAG, "Respuesta ${response.code()}")
        return response
    }

    /** Recetas de una categoria (solo id, nombre y foto). */
    suspend fun getMealsByCategory(category: String): Response<MealResponse> {
        Log.d(TAG, "Pidiendo recetas de la categoria: $category")
        val response = apiService.getMealsByCategory(category)
        Log.d(TAG, "Respuesta ${response.code()}")
        return response
    }

    /** Busqueda por nombre del plato. */
    suspend fun searchByName(name: String): Response<MealResponse> {
        Log.d(TAG, "Buscando por nombre: $name")
        val response = apiService.searchMealsByName(name)
        Log.d(TAG, "Respuesta ${response.code()}")
        return response
    }

    /** Busqueda por ingrediente. La API solo entiende ingredientes en ingles. */
    suspend fun searchByIngredient(ingredient: String): Response<MealResponse> {
        Log.d(TAG, "Buscando por ingrediente: $ingredient")
        val response = apiService.searchMealsByIngredient(ingredient)
        Log.d(TAG, "Respuesta ${response.code()}")
        return response
    }

    /** Receta completa para el detalle. */
    suspend fun getMealById(id: String): Response<MealDetailResponse> {
        Log.d(TAG, "Pidiendo la receta $id")
        val response = apiService.getMealById(id)
        Log.d(TAG, "Respuesta ${response.code()}")
        return response
    }

    /** Receta completa elegida al azar, para el boton del detalle al azar. */
    suspend fun getRandomMeal(): Response<MealDetailResponse> {
        Log.d(TAG, "Pidiendo una receta al azar")
        val response = apiService.getRandomMeal()
        Log.d(TAG, "Respuesta ${response.code()}")
        return response
    }

    companion object {
        private const val TAG = "MealRepository"
    }
}
