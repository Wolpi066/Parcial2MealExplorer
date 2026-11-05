package com.example.parcial2mealexplorer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.ui.theme.MealExplorerTheme

/**
 * Tarjeta "Mis notas". Solo aparece si la receta esta guardada, porque la nota
 * se guarda en la misma fila de SQLite (con un UPDATE).
 *
 * @param savedNote la nota que esta guardada en la base.
 * @param onSaveNote avisa hacia arriba que hay que guardar el texto nuevo.
 */
@Composable
fun NotesCard(savedNote: String, onSaveNote: (String) -> Unit, modifier: Modifier = Modifier) {
    // Estado local con lo que el usuario va escribiendo. rememberSaveable es
    // como remember, pero el texto tambien sobrevive si se rota el celular.
    var noteText by rememberSaveable { mutableStateOf(savedNote) }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.title_notes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text(stringResource(R.string.hint_notes)) },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Si lo escrito es igual a lo guardado, avisamos que ya esta guardada.
                val isSaved = noteText == savedNote && savedNote.isNotEmpty()
                Text(
                    text = if (isSaved) stringResource(R.string.message_note_saved) else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // El boton solo se habilita si hay algo nuevo para guardar.
                Button(
                    onClick = { onSaveNote(noteText) },
                    enabled = noteText != savedNote
                ) {
                    Text(stringResource(R.string.action_save_note))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesCardPreview() {
    MealExplorerTheme {
        NotesCard(savedNote = "Le puse menos sal y quedo mejor.", onSaveNote = {})
    }
}
