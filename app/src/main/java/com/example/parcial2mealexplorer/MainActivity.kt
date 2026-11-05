package com.example.parcial2mealexplorer

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.parcial2mealexplorer.databinding.ActivityMainBinding

/**
 * Pantalla principal. No dibuja recetas por su cuenta: tiene la barra de abajo
 * con las tres pestañas y un contenedor donde pone el Fragment de cada una.
 */
class MainActivity : AppCompatActivity() {

    // View Binding: acceso a las vistas de activity_main.xml sin findViewById.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        setupBottomNavigation()
        setupBackButton()

        // Solo ponemos el primer Fragment la primera vez. Si la pantalla se
        // recrea (al rotar), el FragmentManager ya tiene guardado el que estaba
        // y lo vuelve a poner solo: si no chequearamos esto, quedarian dos encimados.
        if (savedInstanceState == null) {
            showFragment(CategoriesFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_categories -> showFragment(CategoriesFragment())
                R.id.nav_search -> showFragment(SearchFragment())
                R.id.nav_notebook -> showFragment(NotebookFragment())
            }
            // true = marcar la pestaña tocada como seleccionada.
            true
        }

        // Si toca la pestaña en la que ya esta, no hacemos nada (sin esto se
        // recrearia el Fragment y se perderia el scroll).
        binding.bottomNavigation.setOnItemReselectedListener { }
    }

    /**
     * Boton Atras del celular. Como los Fragments se cambian con replace y sin
     * back stack, Atras cerraria la app desde cualquier pestaña. Con este
     * callback, desde Buscar o Mi libreta vuelve a Categorias, y recien desde
     * Categorias sale de la app.
     */
    private fun setupBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNavigation.selectedItemId != R.id.nav_categories) {
                    // Marcar la pestaña dispara el listener de arriba, que pone
                    // el Fragment de categorias.
                    binding.bottomNavigation.selectedItemId = R.id.nav_categories
                } else {
                    finish()
                }
            }
        })
    }

    /** Cambia el Fragment que se ve en el contenedor por el que le pasamos. */
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * Como la app va de punta a punta de la pantalla (edge to edge), agregamos
     * padding para que el contenido no quede debajo de la barra de estado. Abajo
     * no hace falta: BottomNavigationView ya se corre solo por encima de la
     * barra de navegacion del sistema.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
}
