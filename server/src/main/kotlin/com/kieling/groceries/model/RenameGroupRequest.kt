package com.kieling.groceries.model

import kotlinx.serialization.Serializable

@Serializable
data class RenameGroupRequest(
    val oldName: String,
    val newName: String,
)
