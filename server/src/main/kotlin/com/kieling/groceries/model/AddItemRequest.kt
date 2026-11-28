package com.kieling.groceries.model

import kotlinx.serialization.Serializable

@Serializable
data class AddItemRequest(
    val name: String,
    val group: String = "General",
    val addedBy: String? = null,
    val createdAt: Long = 0L,
)
