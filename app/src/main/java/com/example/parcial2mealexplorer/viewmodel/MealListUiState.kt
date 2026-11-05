package com.example.parcial2mealexplorer.viewmodel

import com.example.parcial2mealexplorer.model.Meal

/**
 * Los estados por los que puede pasar una grilla de recetas. Lo usan las tres
 * pestañas: categorias, buscar y mi libreta.
 *
 * Al ser una sealed class, solo pueden heredarla clases del mismo paquete y
 * modulo (aca estan todas en este archivo). Por eso el compilador sabe que son
 * todos los casos posibles y obliga a cubrirlos en el when de cada Fragment.
 */
sealed class MealListUiState {

    /** Todavia no se pidio nada (por ejemplo, el buscador vacio). */
    object Idle : MealListUiState()

    /** Se esta esperando la respuesta de la API o de la base. */
    object Loading : MealListUiState()

    /** Hay recetas para mostrar. */
    data class Success(val meals: List<Meal>) : MealListUiState()

    /** La consulta anduvo bien pero no trajo recetas. */
    object Empty : MealListUiState()

    /**
     * Algo salio mal. Guardamos el id del texto (R.string...) y no el texto en
     * si, porque el ViewModel no tiene Context para leer strings.xml: el
     * Fragment hace el getString.
     */
    data class Error(val messageRes: Int) : MealListUiState()
}
