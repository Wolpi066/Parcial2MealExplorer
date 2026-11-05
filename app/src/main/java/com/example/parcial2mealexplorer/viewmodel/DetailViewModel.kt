package com.example.parcial2mealexplorer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.repository.MealRepository
import com.example.parcial2mealexplorer.repository.NotebookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel del detalle, que esta hecho en Compose.
 *
 * Aca usamos StateFlow en vez de LiveData porque es lo que se vio para Compose:
 * la pantalla lo lee con collectAsState() y se redibuja sola cada vez que cambia.
 *
 * Usa los dos repositorios: la libreta (SQLite) para leer, guardar, quitar y
 * anotar, y la API para las recetas que no estan guardadas.
 */
class DetailViewModel(
    private val mealRepository: MealRepository,
    private val notebookRepository: NotebookRepository
) : ViewModel() {

    // El MutableStateFlow es privado (solo lo toca el ViewModel) y afuera se
    // expone la version de solo lectura.
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Id de la receta que se muestra. Es null cuando se pidio una al azar,
    // hasta que llega la respuesta.
    private var mealId: String? = null

    private var hasStarted = false

    // La corrutina que esta guardando, quitando o anotando en la libreta.
    // Mientras sigue activa, los toques nuevos se ignoran: asi nunca hay dos
    // operaciones usando la base al mismo tiempo.
    private var saveJob: Job? = null

    /**
     * La Activity lo llama en cada onCreate, pero solo la primera vez hace algo.
     * Al rotar, la Activity se recrea y el ViewModel no: ya tiene la receta, y
     * si la pidieramos de nuevo, con la receta al azar apareceria otra distinta.
     */
    fun start(mealId: String?) {
        if (hasStarted) return
        hasStarted = true
        this.mealId = mealId
        loadRecipe()
    }

    /** Boton Reintentar de la pantalla de error. */
    fun retry() {
        loadRecipe()
    }

    /**
     * Orden de carga:
     * 1. si la receta esta en la libreta, sale de SQLite (anda sin internet);
     * 2. si no, se pide a la API por id (lookup.php);
     * 3. si no hay id, se pide una al azar (random.php).
     */
    private fun loadRecipe() {
        _uiState.value = DetailUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = mealId

                if (id != null) {
                    val savedRecipe = notebookRepository.getRecipe(id)
                    if (savedRecipe != null) {
                        _uiState.value = DetailUiState.Success(savedRecipe, isSaved = true)
                        return@launch
                    }
                }

                if (!mealRepository.isNetworkAvailable()) {
                    _uiState.value = DetailUiState.Error(R.string.error_no_connection)
                    return@launch
                }

                val response = if (id == null) {
                    mealRepository.getRandomMeal()
                } else {
                    mealRepository.getMealById(id)
                }

                // La API devuelve una lista con una sola receta, o null si no existe.
                val mealDetail = response.body()?.meals?.firstOrNull()

                if (!response.isSuccessful) {
                    _uiState.value = DetailUiState.Error(R.string.error_server)
                } else if (mealDetail == null) {
                    _uiState.value = DetailUiState.Error(R.string.error_meal_not_found)
                } else {
                    val recipe = mealDetail.toRecipe()
                    // Si era al azar, de aca en adelante trabajamos con esta.
                    mealId = recipe.id

                    // Una receta al azar puede justo estar guardada: en ese caso
                    // usamos la de la libreta, que tiene la nota.
                    val savedRecipe = notebookRepository.getRecipe(recipe.id)
                    if (savedRecipe != null) {
                        _uiState.value = DetailUiState.Success(savedRecipe, isSaved = true)
                    } else {
                        _uiState.value = DetailUiState.Success(recipe, isSaved = false)
                    }
                }
            } catch (e: CancellationException) {
                // Se cancelo porque el usuario cerro la pantalla: no es un error.
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "Fallo la conexion", e)
                _uiState.value = DetailUiState.Error(R.string.error_no_connection)
            } catch (e: Exception) {
                // Un JSON que no se pudo leer o un problema con la base.
                Log.e(TAG, "Error inesperado", e)
                _uiState.value = DetailUiState.Error(R.string.error_unexpected)
            }
        }
    }

    /** Boton Guardar / Quitar: hace el INSERT o el DELETE segun corresponda. */
    fun toggleSaved() {
        val state = _uiState.value
        if (state !is DetailUiState.Success) return

        // Todavia esta terminando el toque anterior: ignoramos este.
        if (saveJob?.isActive == true) return

        saveJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (state.isSaved) {
                    notebookRepository.deleteRecipe(state.recipe.id)
                    // Al quitarla se borra tambien la nota.
                    _uiState.value = DetailUiState.Success(state.recipe.copy(note = ""), isSaved = false)
                } else {
                    val newRowId = notebookRepository.saveRecipe(state.recipe)
                    // insert devuelve -1 si no pudo guardar. En ese caso no
                    // cambiamos nada: la pantalla sigue ofreciendo "Guardar".
                    if (newRowId == -1L) {
                        Log.e(TAG, "No se pudo guardar la receta ${state.recipe.id}")
                    } else {
                        _uiState.value = DetailUiState.Success(state.recipe, isSaved = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo actualizar la libreta", e)
            }
        }
    }

    /** Boton Guardar nota: hace el UPDATE de la columna note. */
    fun saveNote(note: String) {
        val state = _uiState.value
        if (state !is DetailUiState.Success || !state.isSaved) return

        // Mismo control que en toggleSaved: una operacion a la vez.
        if (saveJob?.isActive == true) return

        saveJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                notebookRepository.updateNote(state.recipe.id, note)
                // copy arma una copia del estado cambiando solo la nota.
                _uiState.value = state.copy(recipe = state.recipe.copy(note = note))
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo guardar la nota", e)
            }
        }
    }

    companion object {
        private const val TAG = "DetailViewModel"
    }
}
