package io.github.zorinontop.twojastokrotka.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ShoppingCategory(
    val name: String,
    val icon: ImageVector
)

val shoppingCategories = listOf(
    ShoppingCategory("Owoce i warzywa", Icons.Default.Eco),
    ShoppingCategory("Pieczywo", Icons.Default.BakeryDining),
    ShoppingCategory("Mięso i wędliny", Icons.Default.KebabDining),
    ShoppingCategory("Nabiał", Icons.Default.WaterDrop), // Close enough to milk
    ShoppingCategory("Napoje", Icons.Default.LocalDrink),
    ShoppingCategory("Alkohole i tytoń", Icons.Default.WineBar),
    ShoppingCategory("Słodycze", Icons.Default.Icecream),
    ShoppingCategory("Przekąski", Icons.Default.Fastfood),
    ShoppingCategory("Inne art. spożywcze", Icons.Default.Kitchen),
    ShoppingCategory("Kawy i herbaty", Icons.Default.Coffee),
    ShoppingCategory("Mrożonki i lody", Icons.Default.AcUnit),
    ShoppingCategory("Chemia gosp. i kosmetyki", Icons.Default.CleanHands),
    ShoppingCategory("Art. higieniczne", Icons.Default.Layers),
    ShoppingCategory("Art. dla zwierząt", Icons.Default.Pets),
    ShoppingCategory("Art. przemysłowe", Icons.Default.SoupKitchen),
    ShoppingCategory("Ryby", Icons.Default.SetMeal),
    ShoppingCategory("Inne", Icons.Default.MoreHoriz)
)

fun getIconForCategory(name: String): ImageVector {
    return shoppingCategories.find { it.name == name }?.icon ?: Icons.Default.MoreHoriz
}
