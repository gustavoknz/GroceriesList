package com.kieling.groceries.data.repository

import com.kieling.groceries.data.model.GroceryItemDto
import com.kieling.groceries.data.model.toDomain
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.GroceryRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class AddItemRequest(
    val name: String,
)

class GroceryRepositoryImpl : GroceryRepository {
    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }

    private val baseUrl = "http://localhost:8080"

    private val _items = MutableStateFlow<List<GroceryItem>>(emptyList())
    override val items: StateFlow<List<GroceryItem>> = _items.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        refreshItems()
    }

    private fun refreshItems() {
        scope.launch {
            try {
                val fetched = client.get("$baseUrl/items").body<List<GroceryItemDto>>().map { it.toDomain() }
                _items.value = fetched
            } catch (e: Exception) {
                // Fallback or log if server is not reachable
            }
        }
    }

    override suspend fun getItems(): List<GroceryItem> {
        try {
            val fetched = client.get("$baseUrl/items").body<List<GroceryItemDto>>().map { it.toDomain() }
            _items.value = fetched
            return fetched
        } catch (e: Exception) {
            return _items.value
        }
    }

    override suspend fun addItem(name: String) {
        try {
            val updated =
                client
                    .post("$baseUrl/items") {
                        contentType(ContentType.Application.Json)
                        setBody(AddItemRequest(name))
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            // handle error
        }
    }

    override suspend fun toggleItem(id: Int) {
        try {
            val updated =
                client
                    .post("$baseUrl/items/$id/toggle")
                    .body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            // handle error
        }
    }

    override suspend fun deleteItem(id: Int) {
        try {
            val updated =
                client
                    .delete("$baseUrl/items/$id")
                    .body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            // handle error
        }
    }
}
