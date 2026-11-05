package com.example.parcial2mealexplorer.ui.components

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme
import com.squareup.picasso.Picasso

/**
 * Foto grande del plato, cargada con Picasso.
 *
 * Picasso trabaja con vistas clasicas: su metodo into() recibe un ImageView, y
 * Compose no tiene ImageView. AndroidView es el puente: nos deja meter una vista
 * clasica dentro de una pantalla de Compose.
 * - factory crea el ImageView una sola vez.
 * - update se llama despues y cada vez que cambia la URL, y ahi Picasso carga la foto.
 */
@Composable
fun MealImage(imageUrl: String, modifier: Modifier = Modifier) {
    // En la vista previa de Android Studio Picasso no esta inicializado, asi que
    // ahi mostramos solo un recuadro de color en lugar de la foto.
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer))
        return
    }

    val description = stringResource(R.string.content_description_meal_photo)

    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = description
            }
        },
        update = { imageView ->
            // Si la URL viene vacia, Picasso con null muestra solo el placeholder.
            val url = if (imageUrl.isBlank()) null else imageUrl

            Picasso.get()
                .load(url)
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.placeholder_meal)
                .into(imageView)
        },
        // Con centerCrop la foto es mas grande que el recuadro, y dentro de
        // Compose el ImageView no recorta solo lo que sobra: clipToBounds lo corta.
        modifier = modifier.clipToBounds()
    )
}

@Preview(showBackground = true)
@Composable
private fun MealImagePreview() {
    MealExplorerTheme {
        MealImage(
            imageUrl = "",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
