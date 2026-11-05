package com.example.parcial2mealexplorer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parcial2mealexplorer.R
import com.example.parcial2mealexplorer.databinding.ItemMealBinding
import com.example.parcial2mealexplorer.model.Meal
import com.squareup.picasso.Picasso

/**
 * Conecta una lista de recetas con el RecyclerView: agarra cada Meal y la pega
 * en una tarjeta de item_meal.xml (patron Adapter). Lo usan las tres pestañas.
 *
 * @param mealList lista a mostrar. Arranca vacia y se llena cuando el
 *                 ViewModel publica resultados.
 * @param onItemClick que hacer cuando tocan una tarjeta. Lo decide el Fragment,
 *                    el adapter solo avisa.
 * @param onDeleteClick que hacer cuando tocan el tacho. Es opcional: solo
 *                      Mi libreta lo pasa, y si es null el tacho no se muestra.
 */
class MealAdapter(
    private var mealList: List<Meal>,
    private val onItemClick: (Meal) -> Unit,
    private val onDeleteClick: ((Meal) -> Unit)? = null
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    /**
     * Sostiene las vistas de UNA tarjeta. Guardamos el binding de item_meal.xml
     * en vez de cada vista por separado, asi el RecyclerView recicla la tarjeta
     * sin volver a buscarlas.
     */
    class MealViewHolder(val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root)

    /** Infla item_meal.xml y lo mete en un ViewHolder. Se llama pocas veces. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }

    override fun getItemCount() = mealList.size

    /** Llena la tarjeta de esa posicion con los datos. Se llama muchas veces. */
    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]

        holder.binding.apply {
            tvMealName.text = meal.name

            // Picasso baja la foto en segundo plano, la guarda en cache y la pone
            // en el ImageView. Si la tarjeta se recicla antes de que termine,
            // cancela solo el pedido viejo. placeholder se ve mientras carga y
            // error si no pudo bajarla.
            Picasso.get()
                .load(meal.getMediumThumbnail())
                .placeholder(R.drawable.placeholder_meal)
                .error(R.drawable.placeholder_meal)
                .into(ivMealThumb)

            root.setOnClickListener { onItemClick(meal) }

            if (onDeleteClick != null) {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener { onDeleteClick(meal) }
            } else {
                btnDelete.visibility = View.GONE
            }
        }
    }

    /** Reemplaza la lista y redibuja, cuando el ViewModel publica resultados nuevos. */
    fun updateList(newList: List<Meal>) {
        mealList = newList
        notifyDataSetChanged()
    }
}
