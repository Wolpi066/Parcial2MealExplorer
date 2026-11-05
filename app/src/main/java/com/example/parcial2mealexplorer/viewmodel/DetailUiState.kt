package com.example.parcial2mealexplorer.viewmodel

import com.example.parcial2mealexplorer.model.Recipe

/**
 * Los estados de la pantalla de detalle. Misma idea que MealListUiState: una
 * sealed class, asi el when de DetailScreen tiene que cubrir los tres casos.
 */
sealed class DetailUiState {

    /** Buscando la receta en la libreta o en la API. */
    object Loading : DetailUiState()

    /**
     * La receta llego.
     *
     * @param isSaved si esta en la libreta. Cambia el boton Guardar/Quitar y
     *                hace aparecer el campo de notas.
     */
    data class Success(val recipe: Recipe, val isSaved: Boolean) : DetailUiState()

    /** No se pudo cargar. messageRes es el id del texto en strings.xml. */
    data class Error(val messageRes: Int) : DetailUiState()
}
