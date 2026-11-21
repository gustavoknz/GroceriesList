package com.kieling.groceries

import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.model.AddItemRequest
import com.kieling.groceries.model.RenameGroupRequest
import com.kieling.groceries.model.RenameItemRequest
import io.ktor.server.application.ApplicationCall
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json { prettyPrint = true }
private val dataDirectoryPath = System.getenv("DATA_DIR")
private val dataFile =
    if (dataDirectoryPath != null) {
        File(dataDirectoryPath, "items_by_user.json")
    } else {
        File("items_by_user.json")
    }

private val categoryOrderFile =
    if (dataDirectoryPath != null) {
        File(dataDirectoryPath, "category_order.json")
    } else {
        File("category_order.json")
    }

private val defaultItems =
    listOf(
        GroceryItem(1, "Apples", group = "Fruits"),
        GroceryItem(2, "Milk", group = "Dairy"),
        GroceryItem(3, "Bread", group = "Bakery"),
        GroceryItem(4, "Eggs", group = "Dairy"),
        GroceryItem(5, "Cheese", group = "Dairy"),
        GroceryItem(6, "Coffee", group = "Beverages"),
    )

private val userItemsMap: MutableMap<String, MutableList<GroceryItem>> =
    run {
        if (dataFile.exists()) {
            runCatching {
                json
                    .decodeFromString<Map<String, List<GroceryItem>>>(dataFile.readText())
                    .mapValues { it.value.toMutableList() }
                    .toMutableMap()
            }.getOrDefault(mutableMapOf("default" to defaultItems.toMutableList()))
        } else {
            mutableMapOf("default" to defaultItems.toMutableList())
                .also { saveUserItems(it) }
        }
    }

private val userCategoryOrderMap: MutableMap<String, MutableList<String>> =
    run {
        if (categoryOrderFile.exists()) {
            runCatching {
                json
                    .decodeFromString<Map<String, List<String>>>(categoryOrderFile.readText())
                    .mapValues { it.value.toMutableList() }
                    .toMutableMap()
            }.getOrDefault(mutableMapOf("default" to mutableListOf("Fruits", "Dairy", "Bakery", "Beverages", "General")))
        } else {
            mutableMapOf("default" to mutableListOf("Fruits", "Dairy", "Bakery", "Beverages", "General"))
                .also { saveCategoryOrders(it) }
        }
    }

private fun saveUserItems(map: Map<String, List<GroceryItem>>) {
    runCatching {
        dataFile.writeText(json.encodeToString(map))
    }
}

private fun saveCategoryOrders(map: Map<String, List<String>>) {
    runCatching {
        categoryOrderFile.writeText(json.encodeToString(map))
    }
}

private fun ApplicationCall.getUserId(): String = request.headers["X-User-Id"] ?: request.queryParameters["userId"] ?: "default"

private fun ApplicationCall.getUserItems(): MutableList<GroceryItem> {
    val userId = getUserId()
    return userItemsMap.getOrPut(userId) { defaultItems.toMutableList() }
}

private inline fun ApplicationCall.updateUserItems(
    block: (MutableList<GroceryItem>) -> Unit,
): List<GroceryItem> {
    val list = getUserItems()
    block(list)
    saveUserItems(userItemsMap)
    return list.toList()
}

fun Route.groceryRoutes() {
    staticResources("/", "static", index = "index.html")
    get("/health") {
        call.respondText(sayHello("Ktor"))
    }
    get("/items") {
        call.respond(call.getUserItems().toList())
    }
    get("/categories") {
        val userId = call.getUserId()
        val categories = userCategoryOrderMap.getOrPut(userId) { mutableListOf("Fruits", "Dairy", "Bakery", "Beverages", "General") }
        call.respond(categories)
    }
    put("/categories/reorder") {
        val newOrder = call.receive<List<String>>()
        val userId = call.getUserId()
        userCategoryOrderMap[userId] = newOrder.map { it.trim() }.toMutableList()
        saveCategoryOrders(userCategoryOrderMap)
        call.respond(userCategoryOrderMap[userId]!!)
    }
    post("/categories") {
        val body = call.receive<Map<String, String>>()
        val categoryName = body["name"]?.trim() ?: "General"
        val userId = call.getUserId()
        val categories = userCategoryOrderMap.getOrPut(userId) { mutableListOf("Fruits", "Dairy", "Bakery", "Beverages", "General") }
        if (categoryName.isNotBlank() && !categories.contains(categoryName)) {
            categories.add(categoryName)
            saveCategoryOrders(userCategoryOrderMap)
        }
        call.respond(categories)
    }
    post("/items") {
        val request = call.receive<AddItemRequest>()
        val trimmedName = request.name.trim()
        val normalizedName = trimmedName.lowercase()
        val targetGroup = request.group.trim().ifBlank { "General" }
        val userId = call.getUserId()
        val categories = userCategoryOrderMap.getOrPut(userId) { mutableListOf("Fruits", "Dairy", "Bakery", "Beverages", "General") }
        if (!categories.contains(targetGroup)) {
            categories.add(targetGroup)
            saveCategoryOrders(userCategoryOrderMap)
        }
        val updated =
            call.updateUserItems { list ->
                val existingIndex = list.indexOfFirst { it.name.trim().lowercase() == normalizedName && it.group == targetGroup }
                if (existingIndex != -1) {
                    val existing = list[existingIndex]
                    list[existingIndex] = existing.copy(name = trimmedName, isChecked = false, addedBy = userId)
                } else {
                    val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
                    list.add(GroceryItem(id = newId, name = trimmedName, group = targetGroup, addedBy = userId))
                }
            }
        call.respond(updated)
    }
    post("/items/{id}/toggle") {
        val id = call.parameters["id"]?.toIntOrNull()
        val updated =
            call.updateUserItems { list ->
                if (id != null) {
                    val index = list.indexOfFirst { it.id == id }
                    if (index != -1) {
                        val item = list[index]
                        list[index] = item.copy(isChecked = !item.isChecked)
                    }
                }
            }
        call.respond(updated)
    }
    put("/items/{id}/rename") {
        val id = call.parameters["id"]?.toIntOrNull()
        val request = call.receive<RenameItemRequest>()
        val trimmed = request.newName.trim()
        val updated =
            call.updateUserItems { list ->
                if (id != null && trimmed.isNotBlank()) {
                    val index = list.indexOfFirst { it.id == id }
                    if (index != -1) {
                        list[index] = list[index].copy(name = trimmed)
                    }
                }
            }
        call.respond(updated)
    }
    delete("/items/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        val updated =
            call.updateUserItems { list ->
                if (id != null) {
                    list.removeIf { it.id == id }
                }
            }
        call.respond(updated)
    }
    put("/groups/rename") {
        val request = call.receive<RenameGroupRequest>()
        val userId = call.getUserId()
        val categories = userCategoryOrderMap.getOrPut(userId) { mutableListOf("Fruits", "Dairy", "Bakery", "Beverages", "General") }
        val catIndex = categories.indexOf(request.oldName)
        if (catIndex != -1) {
            categories[catIndex] = request.newName
        } else if (!categories.contains(request.newName)) {
            categories.add(request.newName)
        }
        saveCategoryOrders(userCategoryOrderMap)

        val updated =
            call.updateUserItems { list ->
                list.forEachIndexed { index, item ->
                    if (item.group == request.oldName) {
                        list[index] = item.copy(group = request.newName)
                    }
                }
            }
        call.respond(updated)
    }
}
