package com.kieling.groceries.data.repository

import com.kieling.groceries.Logger
import com.kieling.groceries.data.model.GroceryItemDto
import com.kieling.groceries.data.model.toDomain
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.domain.repository.GroceryRepository
import com.kieling.groceries.getApiBaseUrl
import com.kieling.groceries.getCurrentTimestamp
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Serializable
data class AddItemRequest(
    val name: String,
    val group: String = "General",
    val addedBy: String? = null,
    val createdAt: Long = 0L,
)

@Serializable
data class RenameGroupRequest(
    val oldName: String,
    val newName: String,
)

@Serializable
data class RenameItemRequest(
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

    private val _categoryOrder = MutableStateFlow(listOf("Fruits", "Dairy", "Bakery", "Beverages", "General"))
    override val categoryOrder: StateFlow<List<String>> = _categoryOrder.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollingJob: Job? = null

    init {
        scope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    refreshItems()
                    refreshCategories()
                    startPolling()
                } else {
                    pollingJob?.cancel()
                    _items.value = emptyList()
                }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob =
            scope.launch {
                while (true) {
                    delay(4.seconds) // Poll every 4 seconds for real-time 2-person collaboration
                    refreshItemsSilently()
                    refreshCategoriesSilently()
                }
            }
    }

    private suspend fun refreshItemsSilently() {
        try {
            val fetched =
                client
                    .get("$baseUrl/items") {
                        header("X-User-Id", getUserId())
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = fetched
        } catch (e: Exception) {
            Logger.e(tag, "Error in background sync", e)
        }
    }

    private suspend fun refreshCategories() {
        refreshCategoriesSilently()
    }

    private suspend fun refreshCategoriesSilently() {
        try {
            val fetched =
                client
                    .get("$baseUrl/categories") {
                        header("X-User-Id", getUserId())
                    }.body<List<String>>()
            _categoryOrder.value = fetched
        } catch (e: Exception) {
            Logger.e(tag, "Error syncing categories", e)
        }
    }

    override suspend fun getCategories(): List<String> {
        try {
            val fetched =
                client
                    .get("$baseUrl/categories") {
                        header("X-User-Id", getUserId())
                    }.body<List<String>>()
            _categoryOrder.value = fetched
            return fetched
        } catch (e: Exception) {
            Logger.e(tag, "Error getting categories", e)
            return _categoryOrder.value
        }
    }

    override suspend fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        // Optimistic update
        if (!_categoryOrder.value.contains(trimmed)) {
            _categoryOrder.value += trimmed
        }

        try {
            val updated =
                client
                    .post("$baseUrl/categories") {
                        header("X-User-Id", getUserId())
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("name" to trimmed))
                    }.body<List<String>>()
            _categoryOrder.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error adding category: $trimmed", e)
        }
    }

    override suspend fun reorderCategories(newOrder: List<String>) {
        _categoryOrder.value = newOrder
        try {
            val updated =
                client
                    .put("$baseUrl/categories/reorder") {
                        header("X-User-Id", getUserId())
                        contentType(ContentType.Application.Json)
                        setBody(newOrder)
                    }.body<List<String>>()
            _categoryOrder.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error reordering categories", e)
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

    override suspend fun addItem(
        name: String,
        group: String,
        addedBy: String,
    ) {
        val trimmed = name.trim()
        val targetGroup = group.trim().ifBlank { "General" }
        if (trimmed.isBlank()) return

        val current = _items.value
        val userId = getUserId()
        val author = addedBy.takeIf { it.isNotBlank() } ?: userId
        val timestamp = getCurrentTimestamp()
        val existingIndex = current.indexOfFirst { it.name.trim().equals(trimmed, ignoreCase = true) && it.group == targetGroup }
        val optimisticList =
            if (existingIndex != -1) {
                current.mapIndexed { index, item ->
                    if (index == existingIndex) item.copy(name = trimmed, isChecked = false, addedBy = author) else item
                }
            } else {
                val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
                current + GroceryItem(id = newId, name = trimmed, group = targetGroup, addedBy = author, createdAt = timestamp)
            }
        _items.value = optimisticList

        try {
            val updated =
                client
                    .post("$baseUrl/items") {
                        header("X-User-Id", userId)
                        contentType(ContentType.Application.Json)
                        setBody(AddItemRequest(trimmed, targetGroup, author, timestamp))
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error adding item: $trimmed", e)
        }
    }

    override suspend fun toggleItem(id: Int) {
        val current = _items.value
        _items.value = current.map { if (it.id == id) it.copy(isChecked = !it.isChecked) else it }

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
        val current = _items.value
        _items.value = current.filter { it.id != id }

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

    override suspend fun renameGroup(
        oldName: String,
        newName: String,
    ) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        try {
            val updated =
                client
                    .put("$baseUrl/groups/rename") {
                        header("X-User-Id", getUserId())
                        contentType(ContentType.Application.Json)
                        setBody(RenameGroupRequest(oldName, trimmed))
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error renaming group: $oldName -> $trimmed", e)
        }
    }

    override suspend fun renameItem(
        id: Int,
        newName: String,
    ) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return

        val current = _items.value
        _items.value = current.map { if (it.id == id) it.copy(name = trimmed) else it }

        try {
            val updated =
                client
                    .put("$baseUrl/items/$id/rename") {
                        header("X-User-Id", getUserId())
                        contentType(ContentType.Application.Json)
                        setBody(RenameItemRequest(trimmed))
                    }.body<List<GroceryItemDto>>()
                    .map { it.toDomain() }
            _items.value = updated
        } catch (e: Exception) {
            Logger.e(tag, "Error renaming item: $id -> $trimmed", e)
        }
    }
}
