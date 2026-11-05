package com.example.parcial2mealexplorer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.model.Category
import com.example.parcial2mealexplorer.repository.MealRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel de la pestaña de categorias.
 *
 * Expone dos cosas: la lista de categorias para armar los chips, y el estado
 * de la grilla con las recetas de la categoria elegida.
 *
 * Como se crea con activityViewModels, queda guardado en MainActivity y no en
 * el Fragment: al cambiar de pestaña o rotar el celular sigue siendo el mismo,
 * y no se vuelve a pedir nada.
 */
class CategoriesViewModel(private val repository: MealRepository) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>(emptyList())
    val categories: LiveData<List<Category>> get() = _categories

    private val _uiState = MutableLiveData<MealListUiState>(MealListUiState.Idle)
    val uiState: LiveData<MealListUiState> get() = _uiState

    /**
     * La categoria marcada. Se puede leer desde afuera pero solo la cambia el
     * ViewModel. Sobrevive a la rotacion porque el ViewModel sobrevive.
     */
    var selectedCategory: String? = null
        private set

    // La corrutina del ultimo pedido, para poder cancelarla si el usuario toca
    // otra categoria antes de que llegue la respuesta.
    private var loadJob: Job? = null

    init {
        // Apenas se crea el ViewModel arrancamos a pedir las categorias.
        loadCategories()
    }

    /** Se llama cuando el usuario marca un chip. */
    fun onCategorySelected(category: String) {
        // Al rotar, los chips se vuelven a armar y se vuelve a marcar el mismo:
        // en ese caso no hay nada que pedir.
        if (category == selectedCategory) return

        selectedCategory = category
        loadMeals(category)
    }

    /** Boton Reintentar: repite lo que haya fallado. */
    fun retry() {
        val category = selectedCategory
        if (_categories.value.isNullOrEmpty() || category == null) {
            loadCategories()
        } else {
            loadMeals(category)
        }
    }

    /** Pide las categorias y, si llegan bien, las recetas de la elegida. */
    private fun loadCategories() {
        loadJob?.cancel()
        _uiState.value = MealListUiState.Loading

        // Dispatchers.IO es el grupo de hilos pensado para esperar red o disco.
        loadJob = viewModelScope.launch(Dispatchers.IO) {
            if (!repository.isNetworkAvailable()) {
                _uiState.postValue(MealListUiState.Error(R.string.error_no_connection))
                return@launch
            }

            try {
                val response = repository.getCategories()
                val categoryList = response.body()?.categories

                if (response.isSuccessful && !categoryList.isNullOrEmpty()) {
                    // La primera vez arrancamos con la primera categoria (Beef).
                    val category = selectedCategory ?: categoryList[0].name
                    selectedCategory = category

                    _categories.postValue(categoryList)
                    fetchMeals(category)
                } else {
                    _uiState.postValue(MealListUiState.Error(R.string.error_server))
                }
            } catch (e: CancellationException) {
                // No es un error: se cancelo a proposito. La dejamos seguir.
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Fallo la conexion", e)
                _uiState.postValue(MealListUiState.Error(R.string.error_no_connection))
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado", e)
                _uiState.postValue(MealListUiState.Error(R.string.error_unexpected))
            }
        }
    }

    /** Pide las recetas de una categoria, cancelando el pedido anterior. */
    private fun loadMeals(category: String) {
        loadJob?.cancel()
        _uiState.value = MealListUiState.Loading

        loadJob = viewModelScope.launch(Dispatchers.IO) {
            if (!repository.isNetworkAvailable()) {
                _uiState.postValue(MealListUiState.Error(R.string.error_no_connection))
                return@launch
            }
            fetchMeals(category)
        }
    }

    /** Hace la consulta y traduce lo que vuelve a uno de los estados. */
    private suspend fun fetchMeals(category: String) {
        try {
            val response = repository.getMealsByCategory(category)

            // isSuccessful es true para cualquier codigo 2xx.
            if (response.isSuccessful) {
                // Sin resultados la API manda {"meals": null}, no una lista vacia.
                val meals = response.body()?.meals

                if (meals.isNullOrEmpty()) {
                    _uiState.postValue(MealListUiState.Empty)
                } else {
                    _uiState.postValue(MealListUiState.Success(meals))
                }
            } else {
                _uiState.postValue(MealListUiState.Error(R.string.error_server))
            }
        } catch (e: CancellationException) {
            // El usuario eligio otra categoria mientras esperabamos: no es un
            // error, asi que no mostramos nada y dejamos que se cancele.
            throw e
        } catch (e: IOException) {
            // Una IOException casi siempre es que se corto la conexion o vencio
            // el timeout en el medio de la llamada.
            Log.e(TAG, "Fallo la conexion", e)
            _uiState.postValue(MealListUiState.Error(R.string.error_no_connection))
        } catch (e: Exception) {
            // Cualquier otra cosa suele ser un problema al leer el JSON.
            Log.e(TAG, "Error inesperado", e)
            _uiState.postValue(MealListUiState.Error(R.string.error_unexpected))
        }
    }

    companion object {
        private const val TAG = "CategoriesViewModel"
    }
}
