package com.example.parcial2mealexplorer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme

/**
 * Un paso de la preparacion: el numero en un circulo y el texto al costado.
 *
 * @param number numero del paso, empezando en 1. Lo calcula RecipeContent.
 * @param text lo que hay que hacer en ese paso.
 */
@Composable
fun StepItem(number: Int, text: String, modifier: Modifier = Modifier) {
    // Row: el circulo con el numero a la izquierda y el texto a la derecha.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Surface con forma de circulo y el verde claro del tema de fondo.
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp)
        ) {
            // Box para centrar el numero adentro del circulo.
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        // weight(1f): el texto ocupa todo el ancho que sobra al lado del circulo.
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StepItemPreview() {
    MealExplorerTheme {
        StepItem(number = 1, text = "Preheat oven to 350 F. Spray a 9x13-inch baking pan with non-stick spray.")
    }
}
