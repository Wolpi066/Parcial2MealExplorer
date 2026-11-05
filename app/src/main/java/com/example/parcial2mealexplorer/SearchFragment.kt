package com.example.parcial2mealexplorer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.parcial2mealexplorer.adapter.MealAdapter
import com.example.parcial2mealexplorer.databinding.FragmentSearchBinding
import com.example.parcial2mealexplorer.model.Meal
import com.example.parcial2mealexplorer.repository.MealRepository
import com.example.parcial2mealexplorer.repository.NotebookRepository
import com.example.parcial2mealexplorer.service.NetworkChecker
import com.example.parcial2mealexplorer.viewmodel.MealListUiState
import com.example.parcial2mealexplorer.viewmodel.SearchMode
import com.example.parcial2mealexplorer.viewmodel.SearchViewModel
import com.example.parcial2mealexplorer.viewmodel.ViewModelFactory

/**
 * Pestaña Buscar: un campo de texto con busqueda en tiempo real y un selector
 * para buscar por nombre del plato o por ingrediente. Usa el mismo adapter y la
 * misma grilla que la pestaña de categorias.
 */
class SearchFragment : Fragment() {

    // Mismo esquema de binding que en CategoriesFragment: nullable porque la
    // vista muere antes que el Fragment.
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MealAdapter

    // Atado a MainActivity: al volver a esta pestaña la busqueda sigue ahi.
    private val viewModel: SearchViewModel by activityViewModels {
        ViewModelFactory(
            MealRepository(NetworkChecker(requireContext().applicationContext)),
            NotebookRepository(requireContext().applicationContext)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreSearch()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    /**
     * Al cambiar de pestaña este Fragment se destruye, pero el ViewModel no.
     * Cuando volvemos, ponemos en pantalla el texto y el modo que tenia. Va
     * antes de los listeners para que no disparen una busqueda nueva.
     */
    private fun restoreSearch() {
        binding.etSearch.setText(viewModel.currentQuery)

        if (viewModel.currentMode == SearchMode.INGREDIENT) {
            binding.toggleSearchMode.check(R.id.btnModeIngredient)
        } else {
            binding.toggleSearchMode.check(R.id.btnModeName)
        }
        updateHints(viewModel.currentMode)
    }

    /** Misma grilla y mismo adapter que la pestaña de categorias. */
    private fun setupRecyclerView() {
        adapter = MealAdapter(emptyList(), onItemClick = { meal -> openDetail(meal) })

        val columns = resources.getInteger(R.integer.grid_columns)
        binding.rvResults.layoutManager = GridLayoutManager(requireContext(), columns)
        binding.rvResults.adapter = adapter
    }

    private fun setupListeners() {
        // La busqueda es en tiempo real: no hay boton, el TextWatcher avisa al
        // ViewModel cada vez que cambia el texto y el ViewModel decide cuando
        // salir de verdad a la API.
        binding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No lo necesitamos, pero la interfaz obliga a implementarlo.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Idem.
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s.toString())
            }
        })

        // Se llama al marcar y al desmarcar cada boton; solo nos importa el
        // que queda marcado.
        binding.toggleSearchMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = if (checkedId == R.id.btnModeIngredient) {
                    SearchMode.INGREDIENT
                } else {
                    SearchMode.NAME
                }
                updateHints(mode)
                viewModel.onModeChanged(mode)
            }
        }

        binding.btnRetry.setOnClickListener { viewModel.retry() }
    }

    /** Cambia la pista del campo segun se busque por nombre o por ingrediente. */
    private fun updateHints(mode: SearchMode) {
        if (mode == SearchMode.INGREDIENT) {
            binding.tilSearch.hint = getString(R.string.hint_search_ingredient)
            binding.tilSearch.helperText = getString(R.string.helper_search_ingredient)
        } else {
            binding.tilSearch.hint = getString(R.string.hint_search_name)
            binding.tilSearch.helperText = getString(R.string.helper_search_name)
        }
    }

    /** Un solo observer para toda la grilla, con un when sobre la sealed class. */
    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MealListUiState.Idle ->
                    showMessage(getString(R.string.message_search_start), false)

                is MealListUiState.Loading ->
                    showLoading()

                is MealListUiState.Success ->
                    showResults(state.meals)

                is MealListUiState.Empty ->
                    showMessage(getString(R.string.message_search_empty, viewModel.currentQuery), false)

                is MealListUiState.Error ->
                    showMessage(getString(state.messageRes), true)
            }
        }
    }

    /** Abre el detalle de la receta tocada, mandando su id en el Intent. */
    private fun openDetail(meal: Meal) {
        startActivity(DetailActivity.newIntent(requireContext(), meal.id))
    }

    /** Muestra la grilla y esconde el resto. */
    private fun showResults(meals: List<Meal>) {
        adapter.updateList(meals)
        binding.rvResults.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.layoutMessage.visibility = View.GONE
    }

    /** Prende el indicador de carga y esconde el resto. */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvResults.visibility = View.GONE
        binding.layoutMessage.visibility = View.GONE
    }

    /** Mensaje inicial, de sin resultados o de error. Reintentar solo en errores. */
    private fun showMessage(message: String, showRetry: Boolean) {
        binding.tvMessage.text = message
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
        binding.layoutMessage.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.rvResults.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Soltamos la vista para no dejarla colgada en memoria.
        _binding = null
    }
}
