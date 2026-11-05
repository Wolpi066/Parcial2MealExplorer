package com.example.parcial2mealexplorer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.model.Recipe
import com.example.parcial2mealexplorer.ui.components.ErrorMessage
import com.example.parcial2mealexplorer.ui.components.IngredientItem
import com.example.parcial2mealexplorer.ui.components.MealImage
import com.example.parcial2mealexplorer.ui.components.NotesCard
import com.example.parcial2mealexplorer.ui.components.StepItem
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme
import com.example.parcial2mealexplorer.viewmodel.DetailUiState
import com.example.parcial2mealexplorer.viewmodel.DetailViewModel

/**
 * Pantalla de detalle de una receta, hecha en Jetpack Compose.
 *
 * Lo que tiene que ver con otras apps (abrir el video, compartir) no se hace
 * aca: se avisa hacia arriba con onOpenVideo y onShare, y lo resuelve la Activity.
 *
 * El OptIn es porque TopAppBar todavia esta marcada como experimental en
 * Material 3 (puede cambiar en versiones futuras). Sin el OptIn no compila.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onOpenVideo: (String) -> Unit,
    onShare: (Recipe) -> Unit
) {
    // collectAsState(): se suscribe al StateFlow del ViewModel. Cada vez que el
    // estado cambia, Compose vuelve a dibujar lo que lo usa.
    // 'by': delegado de Kotlin que nos da el valor directo en 'state'.
    val state by viewModel.uiState.collectAsState()

    // Scaffold: estructura base de Material 3 (barra de arriba + contenido).
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_detail)) },
                // Flecha para volver a la pantalla anterior.
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding -> // innerPadding: espacio que deja el Scaffold para no tapar la barra
        // imePadding: cuando se abre el teclado (para escribir la nota) deja
        // abajo el lugar que ocupa, asi la lista se achica y se puede llegar
        // al campo y al boton Guardar nota sin que el teclado los tape.
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()

        // 'state' viene de un delegado (by), y Kotlin no puede hacer smart cast
        // sobre eso. Por eso lo copiamos en currentState dentro del when.
        when (val currentState = state) {
            is DetailUiState.Loading ->
                Box(modifier = contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

            is DetailUiState.Error ->
                ErrorMessage(
                    message = stringResource(currentState.messageRes),
                    onRetry = { viewModel.retry() },
                    modifier = contentModifier
                )

            is DetailUiState.Success ->
                RecipeContent(
                    recipe = currentState.recipe,
                    isSaved = currentState.isSaved,
                    onToggleSaved = { viewModel.toggleSaved() },
                    onSaveNote = { note -> viewModel.saveNote(note) },
                    onOpenVideo = onOpenVideo,
                    onShare = onShare,
                    modifier = contentModifier
                )
        }
    }
}

/**
 * El contenido de la receta. Esta separado de DetailScreen para poder verlo en
 * la vista previa con una receta de ejemplo, sin ViewModel.
 *
 * No guarda estado propio: recibe la receta y avisa con lambdas cuando el
 * usuario toca algo. Quien decide que pasa es el ViewModel o la Activity.
 */
@Composable
fun RecipeContent(
    recipe: Recipe,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onSaveNote: (String) -> Unit,
    onOpenVideo: (String) -> Unit,
    onShare: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    // Los pasos se arman partiendo el texto de las instrucciones (ver Recipe).
    val steps = recipe.getSteps()

    // LazyColumn: lista con scroll que solo dibuja lo que entra en pantalla.
    // Cada item { } es un bloque de la lista.
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Foto grande arriba de todo
        item {
            MealImage(
                imageUrl = recipe.thumbnail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }

        // Nombre, categoria y origen
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Algunas recetas vienen sin categoria o sin origen: ahi no
                    // mostramos la etiqueta vacia.
                    if (recipe.category.isNotBlank()) {
                        InfoLabel(text = stringResource(R.string.label_category, recipe.category))
                    }
                    if (recipe.area.isNotBlank()) {
                        InfoLabel(text = stringResource(R.string.label_area, recipe.area))
                    }
                }
            }
        }

        // Botones: guardar/quitar, ver video y compartir
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // El mismo boton guarda o quita, segun isSaved.
                Button(onClick = onToggleSaved, modifier = Modifier.fillMaxWidth()) {
                    // contentDescription null porque el icono es decorativo: el
                    // texto del boton ya dice lo que hace.
                    Icon(
                        painter = painterResource(if (isSaved) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSaved) {
                            stringResource(R.string.action_remove_from_notebook)
                        } else {
                            stringResource(R.string.action_save_to_notebook)
                        }
                    )
                }

                // weight(1f) en los dos botones: se reparten el ancho por igual.
                // Si no hay video, Compartir ocupa toda la fila.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Algunas recetas no tienen video: en ese caso no hay boton.
                    if (recipe.youtubeUrl.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onOpenVideo(recipe.youtubeUrl) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_play), contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_video))
                        }
                    }
                    OutlinedButton(
                        onClick = { onShare(recipe) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_share), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_share))
                    }
                }
            }
        }

        // Las notas solo existen para las recetas guardadas.
        if (isSaved) {
            item {
                NotesCard(
                    savedNote = recipe.note,
                    onSaveNote = onSaveNote,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )
            }
        }

        item {
            SectionTitle(text = stringResource(R.string.title_ingredients, recipe.ingredients.size))
        }

        // items: un IngredientItem por cada ingrediente de la lista.
        items(recipe.ingredients) { ingredient ->
            IngredientItem(ingredient = ingredient)
        }

        item {
            SectionTitle(text = stringResource(R.string.title_steps))
        }

        // itemsIndexed es como items pero tambien da la posicion, que usamos
        // para numerar los pasos (empieza en 0, por eso el + 1).
        itemsIndexed(steps) { index, step ->
            StepItem(number = index + 1, text = step)
        }
    }
}

/** Etiqueta chica con fondo de color, para la categoria y el origen. */
@Composable
private fun InfoLabel(text: String) {
    // Surface: un fondo con forma y color, como una tarjeta sin sombra.
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/** Titulo de cada seccion (Ingredientes, Preparacion). */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
    )
}

/**
 * Vista previa con una receta inventada, para ver el diseño sin correr la app.
 * heightDp agranda el lienzo para que entre la receta entera.
 */
@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RecipeContentPreview() {
    val sampleRecipe = Recipe(
        id = "1",
        name = "Teriyaki Chicken Casserole",
        category = "Chicken",
        area = "Japanese",
        thumbnail = "",
        instructions = "STEP 1\r\nPreheat oven to 350 F.\r\nCombine soy sauce, water and brown sugar.\r\nBake for 35 minutes.",
        ingredients = listOf("soy sauce: 3/4 cup", "water: 1/2 cup", "brown sugar: 1/4 cup", "chicken breasts: 2"),
        youtubeUrl = "https://www.youtube.com/watch?v=4aZr5hZXP_s",
        note = "Le puse menos azucar."
    )

    MealExplorerTheme {
        RecipeContent(
            recipe = sampleRecipe,
            isSaved = true,
            onToggleSaved = {},
            onSaveNote = {},
            onOpenVideo = {},
            onShare = {}
        )
    }
}
