package com.example.parcial2mealexplorer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parcial2mealexplorer.repository.MealRepository
import com.example.parcial2mealexplorer.repository.NotebookRepository

/**
 * Fabrica de los ViewModels de la app (patron Factory).
 *
 * Hace falta porque ningun ViewModel tiene constructor vacio: todos reciben su
 * repositorio. Android no sabe como armarlos solo, entonces le damos esta clase
 * que le explica como.
 *
 * Este es el punto donde se inyectan las dependencias: la pantalla arma los
 * repositorios, se los pasa a la fabrica, y la fabrica a cada ViewModel.
 */
class ViewModelFactory(
    private val mealRepository: MealRepository,
    private val notebookRepository: NotebookRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Android nos dice que clase de ViewModel necesita y nosotros la armamos.
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(mealRepository) as T
        }
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(mealRepository) as T
        }
        if (modelClass.isAssignableFrom(NotebookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotebookViewModel(notebookRepository) as T
        }
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(mealRepository, notebookRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
