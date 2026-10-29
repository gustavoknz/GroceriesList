package com.kieling.groceries

import com.kieling.groceries.domain.model.GroceryItem
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

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
            try {
                json
                    .decodeFromString<Map<String, List<GroceryItem>>>(dataFile.readText())
                    .mapValues { it.value.toMutableList() }
                    .toMutableMap()
            } catch (_: Exception) {
                mutableMapOf("default" to defaultItems.toMutableList())
            }
        } else {
            mutableMapOf("default" to defaultItems.toMutableList())
                .also { saveUserItems(it) }
        }
    }

private fun saveUserItems(map: Map<String, List<GroceryItem>>) {
    try {
        dataFile.writeText(json.encodeToString(map))
    } catch (_: Exception) {
    }
}

private fun ApplicationCall.getUserId() = request.headers["X-User-Id"] ?: request.queryParameters["userId"] ?: "default"

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
        get("/items") {
            val userId = call.getUserId()
            val list = userItemsMap.getOrPut(userId) { defaultItems.toMutableList() }
            call.respond(list.toList())
        }
        post("/items") {
            val userId = call.getUserId()
            val request = call.receive<AddItemRequest>()
            val list = userItemsMap.getOrPut(userId) { defaultItems.toMutableList() }
            val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
            val newItem = GroceryItem(id = newId, name = request.name.trim())
            list.add(newItem)
            saveUserItems(userItemsMap)
            call.respond(list.toList())
        }
        post("/items/{id}/toggle") {
            val userId = call.getUserId()
            val id = call.parameters["id"]?.toIntOrNull()
            val list = userItemsMap.getOrPut(userId) { defaultItems.toMutableList() }
            if (id != null) {
                val index = list.indexOfFirst { it.id == id }
                if (index != -1) {
                    val item = list[index]
                    list[index] = item.copy(isChecked = !item.isChecked)
                    saveUserItems(userItemsMap)
                }
            }
            call.respond(list.toList())
        }
        delete("/items/{id}") {
            val userId = call.getUserId()
            val id = call.parameters["id"]?.toIntOrNull()
            val list = userItemsMap.getOrPut(userId) { defaultItems.toMutableList() }
            if (id != null) {
                val removed = list.removeIf { it.id == id }
                if (removed) {
                    saveUserItems(userItemsMap)
                }
            }
            call.respond(list.toList())
        }
    }
}
