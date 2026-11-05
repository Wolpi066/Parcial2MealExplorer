package com.example.parcial2mealexplorer.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Crea y versiona la base de datos local de la libreta.
 *
 * SQLiteOpenHelper abre el archivo de la base cuando alguien la pide con
 * writableDatabase o readableDatabase. La primera vez, como el archivo no
 * existe, llama a onCreate. Si despues sube DATABASE_VERSION, llama a onUpgrade.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "meal_explorer.db"

        private const val DATABASE_VERSION = 1

        /** Nombre de la tabla de recetas guardadas. */
        const val TABLE_SAVED_MEALS = "saved_meals"

        // El id es el mismo idMeal de la API (por ejemplo "52772"). Es TEXT
        // porque la API lo manda como texto.
        const val COLUMN_ID = "id"

        const val COLUMN_NAME = "name"

        const val COLUMN_CATEGORY = "category"

        const val COLUMN_AREA = "area"

        const val COLUMN_THUMBNAIL = "thumbnail"

        const val COLUMN_INSTRUCTIONS = "instructions"

        // Los ingredientes van todos juntos en un TEXT, uno por linea. No hace
        // falta una segunda tabla porque nunca se buscan ni se editan por
        // separado: siempre se leen y se guardan junto con su receta.
        const val COLUMN_INGREDIENTS = "ingredients"

        const val COLUMN_YOUTUBE_URL = "youtube_url"

        // Nota personal del usuario. Es lo unico que se modifica con UPDATE.
        const val COLUMN_NOTE = "note"

        // Momento en que se guardo, en milisegundos. Sirve para ordenar la libreta.
        const val COLUMN_SAVED_AT = "saved_at"

        private const val CREATE_TABLE_SAVED_MEALS = """
            CREATE TABLE $TABLE_SAVED_MEALS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_AREA TEXT,
                $COLUMN_THUMBNAIL TEXT,
                $COLUMN_INSTRUCTIONS TEXT,
                $COLUMN_INGREDIENTS TEXT,
                $COLUMN_YOUTUBE_URL TEXT,
                $COLUMN_NOTE TEXT,
                $COLUMN_SAVED_AT INTEGER
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SAVED_MEALS)
    }

    /**
     * Si cambia la estructura de la tabla, se sube DATABASE_VERSION y se pasa
     * por aca. Lo mas simple es borrar la tabla y crearla de nuevo (se pierden
     * las recetas guardadas, cosa aceptable para este trabajo).
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVED_MEALS")
        onCreate(db)
    }
}
