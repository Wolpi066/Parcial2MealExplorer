package com.example.parcial2mealexplorer.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.repository.MealRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel del buscador. Busca mientras el usuario escribe, por nombre o por
 * ingrediente, y expone un solo LiveData con el estado de la grilla.
 *
 * Guarda el texto y el modo actuales para que el Fragment los vuelva a poner
 * cuando el usuario vuelve a esta pestaña.
 */
class SearchViewModel(private val repository: MealRepository) : ViewModel() {

    private val _uiState = MutableLiveData<MealListUiState>(MealListUiState.Idle)
    val uiState: LiveData<MealListUiState> get() = _uiState

    /** Lo ultimo que escribio el usuario, ya sin espacios en las puntas. */
    var currentQuery: String = ""
        private set

    var currentMode: SearchMode = SearchMode.NAME
        private set

    // Guardamos la corrutina de la ultima busqueda para poder cancelarla.
    private var searchJob: Job? = null

    // La ultima busqueda que se hizo (modo + texto). Al rotar, el EditText
    // restaura su texto y el TextWatcher vuelve a avisar lo mismo; con esto
    // evitamos repetir la llamada cuando esos resultados ya estan en pantalla.
    private var lastSearch: String? = null

    /** Se llama en cada tecla que escribe el usuario. */
    fun onQueryChanged(query: String) {
        currentQuery = query.trim()
        startSearch()
    }

    /** Se llama cuando cambia el selector Nombre / Ingrediente. */
    fun onModeChanged(mode: SearchMode) {
        if (mode == currentMode) return
        currentMode = mode
        startSearch()
    }

    /** Boton Reintentar. */
    fun retry() {
        startSearch()
    }

    /**
     * Cancelamos la busqueda anterior antes de lanzar la nueva y esperamos un
     * ratito antes de salir a la API. Asi escribir "chicken" dispara una sola
     * llamada y no una por letra: las anteriores se cancelan durante el delay.
     */
    private fun startSearch() {
        searchJob?.cancel()

        // Con una o dos letras la busqueda no tiene sentido; volvemos al inicio.
        if (currentQuery.length < MIN_QUERY_LENGTH) {
            _uiState.value = MealListUiState.Idle
            return
        }

        // Copias locales: la corrutina usa estos valores aunque despues cambien.
        val query = currentQuery
        val mode = currentMode
        val searchKey = "$mode:$query"

        // Ya tenemos en pantalla la respuesta de esta busqueda (con o sin
        // resultados): no la repetimos. Si fue un error, si se vuelve a buscar.
        val state = _uiState.value
        if (searchKey == lastSearch && (state is MealListUiState.Success || state is MealListUiState.Empty)) {
            return
        }

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(SEARCH_DELAY_MS)
            _uiState.postValue(MealListUiState.Loading)
            search(query, mode)
            lastSearch = searchKey
        }
    }

    /** Hace la consulta al endpoint que corresponde y la traduce a un estado. */
    private suspend fun search(query: String, mode: SearchMode) {
        // Si no hay red ni salimos a preguntar: cortamos aca.
        if (!repository.isNetworkAvailable()) {
            _uiState.postValue(MealListUiState.Error(R.string.error_no_connection))
            return
        }

        try {
            val response = if (mode == SearchMode.NAME) {
                repository.searchByName(query)
            } else {
                repository.searchByIngredient(query)
            }

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
            // El usuario siguio escribiendo mientras esperabamos la respuesta y
            // la busqueda se cancelo: no es un error, la dejamos terminar asi.
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Fallo la conexion", e)
            _uiState.postValue(MealListUiState.Error(R.string.error_no_connection))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado", e)
            _uiState.postValue(MealListUiState.Error(R.string.error_unexpected))
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"

        /** Cuantas letras hacen falta para que valga la pena buscar. */
        private const val MIN_QUERY_LENGTH = 3

        /** Cuanto esperamos, en milisegundos, a que el usuario deje de escribir. */
        private const val SEARCH_DELAY_MS = 400L
    }
}
