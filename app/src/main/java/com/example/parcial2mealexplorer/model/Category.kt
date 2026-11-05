package com.example.parcial2mealexplorer.model

import com.google.gson.annotations.SerializedName

/**
 * Una categoria de platos (Beef, Chicken, Dessert...).
 *
 * La API manda mas datos por categoria (id, foto, descripcion), pero para armar
 * los chips alcanza con el nombre. Gson ignora los campos que no estan aca.
 */
data class Category(

    @SerializedName("strCategory")
    val name: String
)
