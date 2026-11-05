package com.example.parcial2mealexplorer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme

/**
 * Mensaje de error centrado con el boton Reintentar abajo.
 *
 * @param message el texto ya traducido (DetailScreen hace el stringResource).
 * @param onRetry que hacer al tocar Reintentar. El componente no sabe que se
 *                reintenta: solo avisa hacia arriba.
 */
@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    // Column: el texto arriba y el boton abajo, los dos centrados en la pantalla.
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        // Mismo texto "Reintentar" que usan las pantallas XML (strings.xml).
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorMessagePreview() {
    MealExplorerTheme {
        ErrorMessage(
            message = "Sin conexión a internet.",
            onRetry = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
