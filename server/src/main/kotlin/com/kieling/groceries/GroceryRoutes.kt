package com.kieling.groceries

import com.kieling.groceries.domain.model.GroceryItem
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AddItemRequest(
    val name: String,
)

private val json = Json { prettyPrint = true }
private val dataFile = File("items_by_user.json")

private val defaultItems =
    listOf(
        GroceryItem(1, "Apples"),
        GroceryItem(2, "Milk"),
        GroceryItem(3, "Bread"),
        GroceryItem(4, "Eggs"),
        GroceryItem(5, "Cheese"),
        GroceryItem(6, "Coffee"),
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

private fun saveUserItems(map: Map<String, List<GroceryItem>>) {
    runCatching {
        dataFile.writeText(json.encodeToString(map))
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
    get("/") {
        call.respondText(sayHello("Ktor"))
    }
    get("/items") {
        call.respond(call.getUserItems().toList())
    }
    post("/items") {
        val request = call.receive<AddItemRequest>()
        val updated =
            call.updateUserItems { list ->
                val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
                list.add(GroceryItem(id = newId, name = request.name.trim()))
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
}
