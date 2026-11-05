package com.example.parcial2mealexplorer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.parcial2mealexplorer.adapter.MealAdapter
import com.example.parcial2mealexplorer.databinding.FragmentNotebookBinding
import com.example.parcial2mealexplorer.model.Meal
import com.example.parcial2mealexplorer.repository.MealRepository
import com.example.parcial2mealexplorer.repository.NotebookRepository
import com.example.parcial2mealexplorer.service.NetworkChecker
import com.example.parcial2mealexplorer.viewmodel.MealListUiState
import com.example.parcial2mealexplorer.viewmodel.NotebookViewModel
import com.example.parcial2mealexplorer.viewmodel.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Pestaña Mi libreta: las recetas que el usuario guardo en SQLite. Como no
 * depende de la API, es la pestaña que sigue andando sin conexion.
 */
class NotebookFragment : Fragment() {

    // Binding nullable: se crea en onCreateView y se tira en onDestroyView.
    private var _binding: FragmentNotebookBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MealAdapter

    // El dialogo de "Quitar receta" mientras esta abierto. Lo guardamos para
    // poder cerrarlo en onDestroyView (por ejemplo, si se rota con el dialogo
    // abierto).
    private var deleteDialog: AlertDialog? = null

    // La fabrica le pasa al ViewModel el NotebookRepository, que es el de SQLite.
    private val viewModel: NotebookViewModel by activityViewModels {
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
        _binding = FragmentNotebookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    /**
     * onResume se llama cada vez que el Fragment vuelve a estar en primer plano,
     * tambien al volver del detalle. Ahi releemos la libreta para que aparezca
     * lo que se guardo o desaparezca lo que se quito.
     */
    override fun onResume() {
        super.onResume()
        viewModel.loadNotebook()
    }

    private fun setupRecyclerView() {
        // Aca si pasamos onDeleteClick, asi las tarjetas muestran el tacho.
        adapter = MealAdapter(
            emptyList(),
            onItemClick = { meal -> openDetail(meal) },
            onDeleteClick = { meal -> confirmDelete(meal) }
        )

        val columns = resources.getInteger(R.integer.grid_columns)
        binding.rvNotebook.layoutManager = GridLayoutManager(requireContext(), columns)
        binding.rvNotebook.adapter = adapter
    }

    /**
     * Mismo when que en las otras pestañas. Aca nunca hay Reintentar: la
     * libreta no depende de internet, asi que no hay nada que reintentar.
     */
    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MealListUiState.Idle ->
                    showLoading()

                is MealListUiState.Loading ->
                    showLoading()

                is MealListUiState.Success ->
                    showResults(state.meals)

                is MealListUiState.Empty ->
                    showMessage(getString(R.string.message_notebook_empty))

                is MealListUiState.Error ->
                    showMessage(getString(state.messageRes))
            }
        }
    }

    /**
     * Pregunta antes de borrar, porque tambien se pierde la nota personal.
     * MaterialAlertDialogBuilder se usa igual que AlertDialog.Builder, pero el
     * dialogo sale con el estilo Material 3 del resto de la app.
     */
    private fun confirmDelete(meal: Meal) {
        deleteDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, meal.name))
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                viewModel.deleteMeal(meal.id)
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    /** Abre el detalle de la receta tocada, mandando su id en el Intent. */
    private fun openDetail(meal: Meal) {
        startActivity(DetailActivity.newIntent(requireContext(), meal.id))
    }

    /** Muestra la grilla y esconde el resto. */
    private fun showResults(meals: List<Meal>) {
        adapter.updateList(meals)
        binding.rvNotebook.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE
    }

    /** Prende el indicador de carga y esconde el resto. */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvNotebook.visibility = View.GONE
        binding.tvMessage.visibility = View.GONE
    }

    /** Mensaje de libreta vacia (o de error al leer la base). */
    private fun showMessage(message: String) {
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.rvNotebook.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Si el dialogo quedo abierto lo cerramos: si no, al rotar Android avisa
        // que quedo una ventana colgada de una pantalla que ya no existe
        // (WindowLeaked).
        deleteDialog?.dismiss()
        deleteDialog = null

        // Soltamos la vista para no dejarla colgada en memoria.
        _binding = null
    }
}
