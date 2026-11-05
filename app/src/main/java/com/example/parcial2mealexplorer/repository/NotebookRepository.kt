package com.example.parcial2mealexplorer.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.parcial2mealexplorer.data.DatabaseHelper
import com.example.parcial2mealexplorer.model.Meal
import com.example.parcial2mealexplorer.model.Recipe

/**
 * Puerta de entrada a la libreta de recetas guardadas (SQLite).
 *
 * Tiene las cuatro operaciones: guardar (INSERT), leer (SELECT), cambiar la
 * nota (UPDATE) y quitar (DELETE). Las funciones no son suspend: las de SQLite
 * de Android son normales y frenan el hilo mientras trabajan. Por eso los
 * ViewModels las llaman desde Dispatchers.IO y no desde el hilo principal.
 *
 * Cada funcion abre la base y la cierra al final (db.close), como en el ejemplo
 * de clase. Por eso no hay que usar el mismo repositorio desde dos corrutinas a
 * la vez: una podria cerrar la base mientras la otra la usa. El DetailViewModel
 * lo evita haciendo una operacion por vez.
 */
class NotebookRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /** Guarda una receta. Devuelve el id de fila nuevo, o -1 si fallo. */
    fun saveRecipe(recipe: Recipe): Long {
        val db = dbHelper.writableDatabase

        // ContentValues es un mapa columna -> valor para el INSERT.
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, recipe.id)
            put(DatabaseHelper.COLUMN_NAME, recipe.name)
            put(DatabaseHelper.COLUMN_CATEGORY, recipe.category)
            put(DatabaseHelper.COLUMN_AREA, recipe.area)
            put(DatabaseHelper.COLUMN_THUMBNAIL, recipe.thumbnail)
            put(DatabaseHelper.COLUMN_INSTRUCTIONS, recipe.instructions)
            // La lista se guarda como un solo texto, un ingrediente por linea.
            put(DatabaseHelper.COLUMN_INGREDIENTS, recipe.ingredients.joinToString("\n"))
            put(DatabaseHelper.COLUMN_YOUTUBE_URL, recipe.youtubeUrl)
            put(DatabaseHelper.COLUMN_NOTE, recipe.note)
            put(DatabaseHelper.COLUMN_SAVED_AT, System.currentTimeMillis())
        }

        // El segundo parametro (null) solo se usa si values viniera vacio.
        val newRowId = db.insert(DatabaseHelper.TABLE_SAVED_MEALS, null, values)
        db.close()
        return newRowId
    }

    /**
     * Todas las recetas guardadas, las ultimas primero. Para la grilla solo
     * leemos id, nombre y foto.
     */
    fun getSavedMeals(): List<Meal> {
        val mealList = mutableListOf<Meal>()
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_SAVED_MEALS,
            arrayOf(
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_THUMBNAIL
            ), // Columnas que queremos (las demas no hacen falta para la grilla)
            null, // Clausula WHERE (null = todas las filas)
            null, // Argumentos del WHERE
            null, // Clausula GROUP BY
            null, // Clausula HAVING
            "${DatabaseHelper.COLUMN_SAVED_AT} DESC" // ORDER BY: ultimas guardadas primero
        )

        cursor.use { c ->
            // 1. Buscamos la posicion de cada columna dentro del resultado
            val idIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)
            val nameIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)
            val thumbnailIndex = c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_THUMBNAIL)

            // 2. Recorremos fila por fila mientras moveToNext() devuelva true
            while (c.moveToNext()) {
                // 3. Leemos cada campo segun su tipo y su posicion
                val id = c.getString(idIndex)
                val name = c.getString(nameIndex)
                val thumbnail = c.getString(thumbnailIndex)

                // 4. Convertimos la fila en un objeto Meal
                mealList.add(Meal(id, name, thumbnail))
            }
        }

        db.close() // Cerramos la conexion a la base de datos
        return mealList
    }

    /** La receta completa si esta guardada, o null si no esta en la libreta. */
    fun getRecipe(id: String): Recipe? {
        var recipe: Recipe? = null
        val db = dbHelper.readableDatabase

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_SAVED_MEALS,
            null, // null = todas las columnas
            "${DatabaseHelper.COLUMN_ID} = ?", // WHERE id = ?
            arrayOf(id), // El valor que reemplaza al ?
            null, // GROUP BY
            null, // HAVING
            null // ORDER BY
        )

        // cursor.use cierra el Cursor solo al terminar, aunque haya un error.
        cursor.use { c ->
            // Como el id es PRIMARY KEY hay una fila o ninguna, por eso if y no while.
            if (c.moveToFirst()) {
                val ingredientsText =
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENTS)) ?: ""

                recipe = Recipe(
                    id = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                    name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                    category = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)) ?: "",
                    area = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AREA)) ?: "",
                    thumbnail = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_THUMBNAIL)) ?: "",
                    instructions = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INSTRUCTIONS)) ?: "",
                    // Volvemos a partir el texto en una lista, una linea por ingrediente.
                    ingredients = if (ingredientsText.isEmpty()) emptyList() else ingredientsText.split("\n"),
                    youtubeUrl = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_YOUTUBE_URL)) ?: "",
                    note = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE)) ?: ""
                )
            }
        }

        db.close()
        return recipe
    }

    /** Cambia la nota personal de una receta guardada. Devuelve las filas tocadas. */
    fun updateNote(id: String, note: String): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NOTE, note)
        }

        // UPDATE saved_meals SET note = ? WHERE id = ?
        // El id va aparte en arrayOf y no pegado al texto del WHERE: asi SQLite
        // lo trata como un valor y no como SQL (evita la inyeccion SQL).
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_SAVED_MEALS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )

        db.close()
        return rowsAffected
    }

    /** Quita una receta de la libreta. Devuelve las filas borradas. */
    fun deleteRecipe(id: String): Int {
        val db = dbHelper.writableDatabase

        // DELETE FROM saved_meals WHERE id = ?
        val rowsDeleted = db.delete(
            DatabaseHelper.TABLE_SAVED_MEALS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(id)
        )

        db.close()
        return rowsDeleted
    }
}
