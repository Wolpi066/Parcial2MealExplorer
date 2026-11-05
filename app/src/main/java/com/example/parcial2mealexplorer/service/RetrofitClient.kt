package com.example.parcial2mealexplorer.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Unico Retrofit de toda la app (patron Singleton).
 *
 * Al ser un object, Kotlin garantiza que existe una sola instancia. Armar
 * Retrofit tiene su costo, asi que no tiene sentido crear uno por pantalla.
 */
object RetrofitClient {

    // La key "1" es la de prueba que TheMealDB deja usar a cualquiera.
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    /**
     * by lazy: se crea recien la primera vez que alguien lo usa, y despues se
     * devuelve siempre el mismo.
     */
    val apiService: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // GsonConverterFactory es el que convierte el JSON en nuestras data classes.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
