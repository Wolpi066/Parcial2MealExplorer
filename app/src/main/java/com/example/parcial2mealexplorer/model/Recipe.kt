package com.example.parcial2mealexplorer.model

/**
 * Una receta completa como la usa la app: la muestra el detalle y es lo que se
 * guarda en la libreta (SQLite).
 *
 * Sale de dos lugares: de la API (MealDetail.toRecipe()) o de la base de datos
 * (NotebookRepository.getRecipe()).
 */
data class Recipe(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val thumbnail: String,
    val instructions: String,
    // Un elemento por ingrediente, con su medida: "Chicken: 1 lb".
    val ingredients: List<String>,
    val youtubeUrl: String,
    // Nota personal del usuario. Solo existe si la receta esta guardada.
    val note: String = ""
) {

    /**
     * Parte las instrucciones en pasos para numerarlos en pantalla.
     *
     * La API separa los pasos con saltos de linea (\r\n). Muchas recetas traen
     * su propia numeracion ("STEP 1", "Step 2 - ...", "3.", "4)" o un numero
     * solo): la sacamos, porque los numeros ya los ponemos nosotros. Y si la
     * receta vino toda en un solo parrafo, la partimos en oraciones.
     */
    fun getSteps(): List<String> {
        val steps = mutableListOf<String>()

        for (line in instructions.split("\n")) {
            // trim tambien saca el \r que queda al cortar por \n. Algunas
            // recetas usan el caracter "▢" como casilla de verificacion.
            val text = removeStepNumber(line.replace("▢", "").trim())

            // Renglones vacios, o que solo tenian el numero del paso.
            if (text.isEmpty()) continue

            steps.add(text)
        }

        // Si todo vino en un solo parrafo, mostramos un paso por oracion.
        if (steps.size == 1) {
            return splitIntoSentences(steps[0])
        }
        return steps
    }

    /**
     * Saca la numeracion del principio de un renglon. Si no tiene, lo devuelve
     * igual. Por ejemplo: "STEP 1" queda vacio, "Step 2 - Boil the pasta" queda
     * "Boil the pasta", "3. Add salt" queda "Add salt" y "4" queda vacio.
     */
    private fun removeStepNumber(line: String): String {
        var text = line

        // La palabra "step" al principio, sin importar mayusculas.
        val hasStepWord = text.lowercase().startsWith("step")
        if (hasStepWord) {
            text = text.substring(4).trim()
        }

        // Si despues no viene un numero, no era numeracion ("Steam the rice").
        if (text.isEmpty() || !text[0].isDigit()) {
            return line
        }

        // Lo que queda despues del numero: "12. Add salt" queda ". Add salt".
        val afterNumber = text.trimStart { it.isDigit() }

        // Despues de "STEP 1" puede venir " - ", ": " o ". " antes del texto.
        if (hasStepWord) {
            return afterNumber.trimStart { it == ' ' || it == '-' || it == ':' || it == '.' || it == ')' }
        }

        // Sin la palabra step, solo es numeracion si es el numero solo ("4") o
        // si sigue un punto o un parentesis ("3. Add salt", "5) Bake",
        // "01.Put"). Si despues del punto viene otro numero es una cantidad
        // ("1.5 kg"), y "2 cups of water..." tampoco se toca.
        if (afterNumber.isEmpty()) {
            return ""
        }
        val sign = afterNumber[0]
        val rest = afterNumber.substring(1)
        if ((sign == '.' || sign == ')') && (rest.isEmpty() || !rest[0].isDigit())) {
            return rest.trim()
        }
        return line
    }

    /**
     * Parte un parrafo en oraciones cortando en cada ". ". split se come el
     * punto, asi que se lo volvemos a poner a cada oracion.
     */
    private fun splitIntoSentences(paragraph: String): List<String> {
        val sentences = mutableListOf<String>()

        for (part in paragraph.split(". ")) {
            val sentence = part.trim()
            if (sentence.isEmpty()) continue

            if (sentence.endsWith(".") || sentence.endsWith("!")) {
                sentences.add(sentence)
            } else {
                sentences.add("$sentence.")
            }
        }
        return sentences
    }

    /** Pagina de la receta en TheMealDB. Es el link que va al compartir. */
    fun getWebPage(): String = "https://www.themealdb.com/meal/$id"
}
