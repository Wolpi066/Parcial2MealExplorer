package com.example.parcial2mealexplorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme

/** Un renglon de la lista de ingredientes: un punto verde y el texto. */
@Composable
fun IngredientItem(ingredient: String, modifier: Modifier = Modifier) {
    // Row: el punto y el texto uno al lado del otro.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // El punto es un Box vacio de 8dp con fondo redondo del color principal.
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        // Spacer: espacio vacio entre el punto y el texto.
        Spacer(modifier = Modifier.width(12.dp))
        // El texto ya viene armado como "Chicken: 1 lb" desde el modelo.
        Text(
            text = ingredient,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientItemPreview() {
    MealExplorerTheme {
        IngredientItem(ingredient = "Chicken Breast: 2 cups")
    }
}
