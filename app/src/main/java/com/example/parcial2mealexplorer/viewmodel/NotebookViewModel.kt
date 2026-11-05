package com.example.parcial2mealexplorer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel de Mi libreta. Lee las recetas guardadas en SQLite, asi que
 * funciona igual con o sin internet.
 */
class NotebookViewModel(private val repository: NotebookRepository) : ViewModel() {

    private val _uiState = MutableLiveData<MealListUiState>(MealListUiState.Idle)
    val uiState: LiveData<MealListUiState> get() = _uiState

    /**
     * Vuelve a leer la libreta. El Fragment la llama en onResume, asi se ve
     * actualizada cuando volvemos del detalle despues de guardar o quitar algo.
     */
    fun loadNotebook() {
        // La base es un archivo en el disco: tambien va en Dispatchers.IO para
        // no trabar el hilo principal.
        viewModelScope.launch(Dispatchers.IO) {
            readMeals()
        }
    }

    /** Quita una receta (DELETE) y vuelve a leer la lista. */
    fun deleteMeal(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteRecipe(id)
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo borrar la receta $id", e)
            }
            readMeals()
        }
    }

    private fun readMeals() {
        try {
            val meals = repository.getSavedMeals()

            if (meals.isEmpty()) {
                _uiState.postValue(MealListUiState.Empty)
            } else {
                _uiState.postValue(MealListUiState.Success(meals))
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo leer la libreta", e)
            _uiState.postValue(MealListUiState.Error(R.string.error_database))
        }
    }

    companion object {
        private const val TAG = "NotebookViewModel"
    }
}
