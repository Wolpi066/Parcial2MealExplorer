package com.example.parcial2mealexplorer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.parcial2mealexplorer.model.Recipe
import com.example.parcial2mealexplorer.repository.MealRepository
import com.example.parcial2mealexplorer.repository.NotebookRepository
import com.example.parcial2mealexplorer.service.NetworkChecker
import com.example.parcial2mealexplorer.ui.DetailScreen
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme
import com.example.parcial2mealexplorer.viewmodel.DetailViewModel
import com.example.parcial2mealexplorer.viewmodel.ViewModelFactory

/**
 * Pantalla de detalle. A diferencia de las otras, no usa XML: es una
 * ComponentActivity y dibuja todo con Jetpack Compose (DetailScreen).
 *
 * Recibe el id de la receta por Intent. Si no recibe id, muestra una al azar
 * (es lo que hace el boton "¿Que cocino hoy?").
 */
class DetailActivity : ComponentActivity() {

    // Mismo esquema que en los Fragments: la Activity arma los repositorios y
    // la fabrica se los pasa al ViewModel.
    private val viewModel: DetailViewModel by viewModels {
        ViewModelFactory(
            MealRepository(NetworkChecker(applicationContext)),
            NotebookRepository(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // getStringExtra devuelve null si no vino nada con esa clave.
        val mealId = intent.getStringExtra(EXTRA_MEAL_ID)
        viewModel.start(mealId)

        // setContent reemplaza al setContentView del XML: lo de adentro es Compose.
        setContent {
            MealExplorerTheme {
                DetailScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onOpenVideo = { url -> openVideo(url) },
                    onShare = { recipe -> shareRecipe(recipe) }
                )
            }
        }
    }

    /** Abre el video de YouTube con la app que el celular tenga para eso. */
    private fun openVideo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Por si no hay ni YouTube ni navegador instalados.
            Toast.makeText(this, R.string.error_no_video_app, Toast.LENGTH_SHORT).show()
        }
    }

    /** Comparte la receta como texto: nombre, ingredientes y link. */
    private fun shareRecipe(recipe: Recipe) {
        val ingredients = recipe.ingredients.joinToString("\n") { "- $it" }
        val text = getString(R.string.share_text, recipe.name, ingredients, recipe.getWebPage())

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)

        // createChooser muestra la lista de apps (WhatsApp, Gmail...) para elegir.
        startActivity(Intent.createChooser(intent, getString(R.string.share_chooser_title)))
    }

    companion object {
        /**
         * Clave del Intent. Va en el companion object para que el que guarda y
         * el que lee usen exactamente el mismo texto.
         */
        const val EXTRA_MEAL_ID = "extra_meal_id"

        /**
         * Arma el Intent para abrir esta pantalla. Lo usan las tres pestañas, asi
         * la clave del extra se escribe en un solo lugar.
         *
         * @param mealId id de la receta, o null para una al azar.
         */
        fun newIntent(context: Context, mealId: String?): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(EXTRA_MEAL_ID, mealId)
            return intent
        }
    }
}
