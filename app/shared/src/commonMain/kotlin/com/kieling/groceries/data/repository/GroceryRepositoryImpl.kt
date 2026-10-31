package com.kieling.groceries.data.repository

import com.kieling.groceries.Logger
import com.kieling.groceries.data.model.GroceryItemDto
import com.kieling.groceries.data.model.toDomain
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.domain.repository.GroceryRepository
import com.kieling.groceries.getApiBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
    val group: String = "General",
)

@Serializable
data class RenameGroupRequest(
    val oldName: String,
    val newName: String,
)

class GroceryRepositoryImpl(
    private val client: HttpClient,
    private val authRepository: AuthRepository,
) : GroceryRepository {
    private val tag = "GroceryRepositoryImpl"
    private val baseUrl = getApiBaseUrl()

    private val _items = MutableStateFlow<List<GroceryItem>>(emptyList())
    override val items: StateFlow<List<GroceryItem>> = _items.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            authRepository.currentUser.collect { _ ->
                refreshItems()
            }
        }
    }

    private fun getUserId(): String = authRepository.currentUser.value?.uid ?: "default"

    private fun refreshItems() {
        scope.launch {
            try {
                val fetched =
                    client
                        .get("$baseUrl/items") {
                            header("X-User-Id", getUserId())
                        }.body<List<GroceryItemDto>>()
                        .map { it.toDomain() }
                _items.value = fetched
            } catch (e: Exception) {
                Logger.e(tag, "Error refreshing items", e)
            }
        }
    }

    override suspend fun getItems(): List<GroceryItem> {
        try {
            val fetched =
                client
                    .get("$baseUrl/items") {
                        header("X-User-Id", getUserId())
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = fetched
            return fetched
        } catch (e: Exception) {
            Logger.e(tag, "Error getting items", e)
            return _items.value
        }
    }

    override suspend fun addItem(name: String, group: String) {
        try {
            val updated =
                client
                    .post("$baseUrl/items") {
                        header("X-User-Id", getUserId())
                        contentType(ContentType.Application.Json)
                        setBody(AddItemRequest(name, group))
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error adding item: $name", e)
        }
    }

    override suspend fun toggleItem(id: Int) {
        try {
            val updated =
                client
                    .post("$baseUrl/items/$id/toggle") {
                        header("X-User-Id", getUserId())
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error toggling item: $id", e)
        }
    }

    override suspend fun deleteItem(id: Int) {
        try {
            val updated =
                client
                    .delete("$baseUrl/items/$id") {
                        header("X-User-Id", getUserId())
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error deleting item: $id", e)
        }
    }

    override suspend fun renameGroup(oldName: String, newName: String) {
        try {
            val updated =
                client
                    .put("$baseUrl/groups/rename") {
                        header("X-User-Id", getUserId())
                        contentType(ContentType.Application.Json)
                        setBody(RenameGroupRequest(oldName, newName))
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error renaming group: $oldName -> $newName", e)
        }
    }
}
