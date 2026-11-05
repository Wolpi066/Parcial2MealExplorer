package com.example.parcial2mealexplorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.parcial2mealexplorer.adapter.MealAdapter
import com.example.parcial2mealexplorer.databinding.FragmentCategoriesBinding
import com.example.parcial2mealexplorer.model.Category
import com.example.parcial2mealexplorer.model.Meal
import com.example.parcial2mealexplorer.repository.MealRepository
import com.example.parcial2mealexplorer.repository.NotebookRepository
import com.example.parcial2mealexplorer.service.NetworkChecker
import com.example.parcial2mealexplorer.viewmodel.CategoriesViewModel
import com.example.parcial2mealexplorer.viewmodel.MealListUiState
import com.example.parcial2mealexplorer.viewmodel.ViewModelFactory
import com.google.android.material.chip.Chip

/**
 * Pestaña principal: chips con las categorias arriba y la grilla de recetas
 * de la categoria elegida abajo.
 *
 * Solo muestra y escucha al usuario. Los pedidos a la API los hace el
 * ViewModel; este Fragment observa los estados y dibuja.
 */
class CategoriesFragment : Fragment() {

    // El binding se crea en onCreateView y se tira en onDestroyView, porque la
    // vista del fragment puede morir antes que el fragment. Por eso es nullable.
    private var _binding: FragmentCategoriesBinding? = null

    // Atajo para no escribir _binding!! en todos lados. Solo valido mientras la
    // vista existe (entre onCreateView y onDestroyView).
    private val binding get() = _binding!!

    private lateinit var adapter: MealAdapter

    // activityViewModels: el ViewModel queda atado a MainActivity y no a este
    // Fragment. Asi, al cambiar de pestaña (que destruye el Fragment) los datos
    // siguen ahi cuando volvemos.
    //
    // Aca se arman las dependencias y se inyectan a traves de la fabrica. La
    // lambda corre cada vez que se crea este Fragment (al cambiar de pestaña o
    // rotar) y arma una fabrica nueva, que es barata. Pero create() de la
    // fabrica solo se llama la primera vez: despues Android devuelve el
    // ViewModel que ya existe.
    private val viewModel: CategoriesViewModel by activityViewModels {
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
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    /** Grilla de 2 columnas (6 en horizontal) con el adapter enganchado. */
    private fun setupRecyclerView() {
        adapter = MealAdapter(emptyList(), onItemClick = { meal -> openDetail(meal) })

        val columns = resources.getInteger(R.integer.grid_columns)
        binding.rvMeals.layoutManager = GridLayoutManager(requireContext(), columns)
        binding.rvMeals.adapter = adapter
    }

    private fun setupListeners() {
        // Se dispara cada vez que cambia el chip marcado.
        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                viewModel.onCategorySelected(chip.text.toString())
            }
        }

        binding.btnRetry.setOnClickListener { viewModel.retry() }

        // "¿Que cocino hoy?": abre el detalle sin id, y el detalle pide una
        // receta al azar.
        binding.fabRandom.setOnClickListener {
            startActivity(DetailActivity.newIntent(requireContext(), null))
        }
    }

    /**
     * Dos observers: uno arma los chips cuando llegan las categorias y el otro
     * dibuja la grilla segun el estado. viewLifecycleOwner hace que dejen de
     * escuchar cuando se destruye la vista.
     */
    private fun setupObservers() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            showCategories(categories)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MealListUiState.Idle ->
                    showLoading()

                is MealListUiState.Loading ->
                    showLoading()

                is MealListUiState.Success ->
                    showResults(state.meals)

                is MealListUiState.Empty ->
                    showMessage(getString(R.string.message_categories_empty), false)

                is MealListUiState.Error ->
                    showMessage(getString(state.messageRes), true)
            }
        }
    }

    /** Crea un chip por categoria y marca la que esta elegida en el ViewModel. */
    private fun showCategories(categories: List<Category>) {
        binding.chipGroupCategories.removeAllViews()

        for (category in categories) {
            val chip = layoutInflater.inflate(
                R.layout.item_category_chip,
                binding.chipGroupCategories,
                false
            ) as Chip

            chip.text = category.name
            // El ChipGroup necesita que cada chip tenga un id para saber cual
            // esta marcado. Como los creamos por codigo, generamos uno.
            chip.id = View.generateViewId()
            binding.chipGroupCategories.addView(chip)

            if (category.name == viewModel.selectedCategory) {
                chip.isChecked = true
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
        binding.rvMeals.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.layoutMessage.visibility = View.GONE
    }

    /** Prende el indicador de carga y esconde el resto. */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvMeals.visibility = View.GONE
        binding.layoutMessage.visibility = View.GONE
    }

    /** Mismo lugar para el mensaje de vacio y el de error. Reintentar solo en errores. */
    private fun showMessage(message: String, showRetry: Boolean) {
        binding.tvMessage.text = message
        binding.btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
        binding.layoutMessage.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.rvMeals.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para no dejar la vista colgada en memoria.
        _binding = null
    }
}
