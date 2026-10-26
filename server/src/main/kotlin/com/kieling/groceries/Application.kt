package com.kieling.groceries

import com.kieling.groceries.domain.model.GroceryItem
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
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
private val dataFile = File("items.json")

private val itemsList: MutableList<GroceryItem> =
    run {
        if (dataFile.exists()) {
            try {
                json.decodeFromString<List<GroceryItem>>(dataFile.readText()).toMutableList()
            } catch (_: Exception) {
                mutableListOf(
                    GroceryItem(1, "Apples"),
                    GroceryItem(2, "Milk"),
                    GroceryItem(3, "Bread"),
                    GroceryItem(4, "Eggs"),
                    GroceryItem(5, "Cheese"),
                    GroceryItem(6, "Coffee"),
                )
            }
        } else {
            mutableListOf(
                GroceryItem(1, "Apples"),
                GroceryItem(2, "Milk"),
                GroceryItem(3, "Bread"),
                GroceryItem(4, "Eggs"),
                GroceryItem(5, "Cheese"),
                GroceryItem(6, "Coffee"),
            ).also { saveItems(it) }
        }
    }

private fun saveItems(items: List<GroceryItem>) {
    try {
        dataFile.writeText(json.encodeToString(items))
    } catch (_: Exception) {
    }
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
        get("/items") {
            call.respond(itemsList.toList())
        }
        post("/items") {
            val request = call.receive<AddItemRequest>()
            val newId = (itemsList.maxOfOrNull { it.id } ?: 0) + 1
            val newItem = GroceryItem(id = newId, name = request.name.trim())
            itemsList.add(newItem)
            saveItems(itemsList)
            call.respond(itemsList.toList())
        }
        post("/items/{id}/toggle") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val index = itemsList.indexOfFirst { it.id == id }
                if (index != -1) {
                    val item = itemsList[index]
                    itemsList[index] = item.copy(isChecked = !item.isChecked)
                    saveItems(itemsList)
                }
            }
            call.respond(itemsList.toList())
        }
        delete("/items/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val removed = itemsList.removeIf { it.id == id }
                if (removed) {
                    saveItems(itemsList)
                }
            }
            call.respond(itemsList.toList())
        }
    }
}
