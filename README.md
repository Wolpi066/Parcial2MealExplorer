# MealExplorer

App de recetas hecha en Kotlin. Usa la API pública de TheMealDB para ver platos
por categoría, buscarlos y ver cómo se preparan. Las recetas que más te gustan
se pueden guardar en "Mi libreta", que funciona sin internet.

Segundo examen de Desarrollo de Aplicaciones Android con Kotlin, Tema 2
Emiliano Volpino

## Video demostrativo

**[Ver video](https://drive.google.com/file/d/1JOSHFw_esjCFxxSXXrL1pWcMWHJRSEMi/view?usp=sharing)**

## La API

Base: `https://www.themealdb.com/api/json/v1/1/` (la key `1` es la de prueba).

| Endpoint | Para qué lo uso |
|---|---|
| `categories.php` | las 14 categorías |
| `filter.php?c=Beef` | las recetas de una categoría |
| `search.php?s=curry` | buscar por nombre |
| `filter.php?i=chicken` | buscar por ingrediente |
| `lookup.php?i=52772` | la receta completa |
| `random.php` | una receta al azar |

Cosas de la API que tuve que tener en cuenta:

- Cuando no hay resultados devuelve `{"meals": null}` y no una lista vacía.
- Los ingredientes vienen en 20 campos sueltos (`strIngredient1` a
  `strIngredient20`) y las medidas en otros 20. Los junto en una lista en
  `MealDetail.getIngredients()`.
- Las fotos tienen varios tamaños agregando algo al final de la URL: `/small`
  (150x150), `/medium` (350x350) y `/large` (500x500). En la grilla uso
  `/medium` y en el detalle la original.
- Las instrucciones a veces traen su propia numeración ("STEP 1", "1.") o
  vienen en un solo párrafo. `Recipe.getSteps()` saca esa numeración y parte el
  párrafo en oraciones.
- Todo viene en inglés, y la búsqueda por ingrediente solo entiende inglés
  ("chicken" sí, "pollo" no).

## Pantallas

- **Categorías** (Fragment + XML): chips con las categorías y una grilla con
  foto y nombre. El botón "¿Qué cocino hoy?" abre una receta al azar.
- **Buscar** (Fragment + XML): busca mientras escribís, por nombre o por
  ingrediente.
- **Mi libreta** (Fragment + XML): las recetas guardadas en SQLite. Se pueden
  quitar (pide confirmación).
- **Detalle** (Jetpack Compose): foto, categoría, origen, ingredientes y pasos
  numerados. Botones para guardar o quitar de la libreta, ver el video en
  YouTube y compartir. Si la receta está guardada se le puede agregar una nota.

Las tres primeras están en `MainActivity`, con una barra de navegación abajo.
El botón Atrás desde Buscar o Mi libreta vuelve a Categorías.

## Cómo está organizado

Uso MVVM, como en el primer parcial:

```
model/        las clases de datos (Meal, MealDetail, Recipe...)
service/      Retrofit, la interfaz de la API y NetworkChecker
data/         DatabaseHelper (SQLite)
repository/   MealRepository (API) y NotebookRepository (SQLite)
adapter/      MealAdapter, el adapter del RecyclerView
viewmodel/    los ViewModels, sus estados y la fábrica
ui/           la pantalla de detalle en Compose, sus componentes y el tema
```

Las Activities y los Fragments están en la carpeta principal.

Cada pantalla muestra un estado hecho con `sealed class`: las grillas usan
`MealListUiState` (Idle, Loading, Success, Empty, Error) y el detalle
`DetailUiState` (Loading, Success, Error). Categorías además observa la lista
de categorías para armar los chips.

## Algunas decisiones

- **LiveData en las pantallas XML y StateFlow en Compose**, porque es lo que
  vimos en clase para cada caso.
- **Los ViewModels de las pestañas usan `activityViewModels`**, así al cambiar
  de pestaña no se pierde la categoría elegida ni la búsqueda.
- **El detalle busca primero en SQLite**: si la receta está guardada la muestra
  aunque no haya internet. Si no, la pide a la API.
- **Búsqueda con debounce**: espera 400 ms desde la última tecla antes de
  buscar, así no hace una llamada por cada letra.
- **Al rotar no se repiten los pedidos**, porque los datos están en los
  ViewModels.
- **Los ingredientes se guardan en una sola columna**, uno por línea, porque
  siempre se leen junto con la receta.
- **Picasso en Compose**: Picasso carga en un `ImageView`, así que en el detalle
  lo uso dentro de un `AndroidView`.
- **Guardar o quitar de a una vez**: mientras se está guardando, los toques
  nuevos se ignoran, para que no haya dos operaciones usando la base al mismo
  tiempo.

## Manejo de errores

| Qué pasa | Qué se ve |
|---|---|
| No hay internet | "Sin conexión a internet..." y el botón Reintentar |
| Se corta la conexión en medio | lo mismo (se atrapa la `IOException`) |
| El servidor responde con error | "El servidor no respondió bien" y Reintentar |
| Otro error | "Ocurrió un error inesperado" y Reintentar |
| No hay resultados | "No encontramos recetas para..." |
| Mi libreta vacía | un texto que explica cómo guardar |
| Receta guardada, sin internet | se ve igual, sale de SQLite |
| Falla al guardar en la base | no se marca como guardada y queda en el Logcat |
| Una foto que no baja | el dibujo de cubiertos (placeholder de Picasso) |
| Receta sin video | no aparece el botón |
| No hay app para abrir el video | un aviso, sin que se cierre la app |

Antes de llamar a la API pregunto con `NetworkChecker` si hay red. Los errores
quedan en el Logcat con `Log.e`.

## Patrones de diseño

- **MVVM**: `viewmodel/` con los Fragments y el detalle.
- **Repository**: `MealRepository` y `NotebookRepository`.
- **Singleton**: `RetrofitClient` es un `object`.
- **Factory**: `ViewModelFactory`.
- **Adapter**: `MealAdapter`.
- **Observer**: `LiveData` con `observe` y `StateFlow` con `collectAsState`.
- **Estados con sealed class**: `MealListUiState` y `DetailUiState`.

## Tecnologías

Kotlin, corrutinas, Retrofit 3.0.0 + Gson, ViewModel, LiveData, StateFlow,
RecyclerView, View Binding, Fragments, SQLite, Jetpack Compose, Picasso 2.8 y
Material 3 con modo oscuro.

## Cómo correrlo

1. Abrir la carpeta en Android Studio y esperar el Gradle Sync.
2. Elegir un emulador o un celular con Android 7.0 (API 24) o más nuevo.
3. Darle a Run.

Hace falta internet para el sync y para la API.
