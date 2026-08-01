package io.github.zorinontop.twojastokrotka.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

data class LocalShoppingList(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val items: MutableList<LocalShoppingItem> = mutableListOf(),
    val dateCreated: Long = System.currentTimeMillis()
)

data class LocalShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var categoryName: String = "Inne",
    var isChecked: Boolean = false
)

class ShoppingManager(private val context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "shopping_lists.json")

    fun getLists(): List<LocalShoppingList> {
        return if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<LocalShoppingList>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun saveLists(lists: List<LocalShoppingList>) {
        val json = gson.toJson(lists)
        file.writeText(json)
    }

    fun addList(name: String): LocalShoppingList {
        val lists = getLists().toMutableList()
        val newList = LocalShoppingList(name = name)
        lists.add(0, newList)
        saveLists(lists)
        return newList
    }

    fun deleteList(listId: String) {
        val lists = getLists().toMutableList()
        lists.removeAll { it.id == listId }
        saveLists(lists)
    }

    fun renameList(listId: String, newName: String) {
        val lists = getLists()
        val list = lists.find { it.id == listId }
        list?.name = newName
        saveLists(lists)
    }

    fun addItem(listId: String, name: String, category: String) {
        val lists = getLists()
        val list = lists.find { it.id == listId }
        list?.items?.add(0, LocalShoppingItem(name = name, categoryName = category))
        saveLists(lists)
    }

    fun toggleItem(listId: String, itemId: String, isChecked: Boolean) {
        val lists = getLists()
        val list = lists.find { it.id == listId }
        val item = list?.items?.find { it.id == itemId }
        item?.isChecked = isChecked
        saveLists(lists)
    }

    fun renameItem(listId: String, itemId: String, newName: String) {
        val lists = getLists()
        val list = lists.find { it.id == listId }
        val item = list?.items?.find { it.id == itemId }
        item?.name = newName
        saveLists(lists)
    }

    fun deleteItem(listId: String, itemId: String) {
        val lists = getLists()
        val list = lists.find { it.id == listId }
        list?.items?.removeAll { it.id == itemId }
        saveLists(lists)
    }
}
