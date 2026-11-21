package com.kieling.groceries.model

import kotlinx.serialization.Serializable

@Serializable
data class AddItemRequest(
    val name: String,
    val group: String = "General",
)
