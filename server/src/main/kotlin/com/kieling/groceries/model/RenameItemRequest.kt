package com.kieling.groceries.model

import kotlinx.serialization.Serializable

@Serializable
data class RenameItemRequest(
    val newName: String,
)
