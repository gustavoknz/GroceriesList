package com.kieling.groceries.presentation.model

import androidx.compose.runtime.Immutable
import com.kieling.groceries.domain.model.GroceryGroup
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.formatDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class GroceryItemUi(
    val id: Int,
    val name: String,
    val isChecked: Boolean,
    val group: String,
    val addedBy: String = "",
    val createdAt: Long = 0L,
    val authorDisplay: String = "",
)

@Immutable
data class GroceryGroupUi(
    val name: String,
    val items: ImmutableList<GroceryItemUi>,
)

fun formatUserInitials(fullName: String): String {
    val trimmed = fullName.trim()
    if (trimmed.isBlank()) return ""
    val parts = trimmed.split(Regex("\\s+"))
    return if (parts.size >= 2) {
        val first = parts.first().firstOrNull()?.uppercase() ?: ""
        val last = parts.last().firstOrNull()?.uppercase() ?: ""
        "$first$last"
    } else {
        val name = parts[0]
        if (name.length >= 2) {
            "${name[0].uppercase()}${name[1].lowercase()}"
        } else {
            name.uppercase()
        }
    }
}

fun GroceryItem.toUi(): GroceryItemUi {
    val initials = formatUserInitials(addedBy)
    val dateStr = if (createdAt > 0L) formatDate(createdAt) else ""
    val authorDisplay =
        if (initials.isNotBlank()) {
            if (dateStr.isNotBlank()) "$initials • $dateStr" else initials
        } else {
            ""
        }
    return GroceryItemUi(
        id = id,
        name = name,
        isChecked = isChecked,
        group = group,
        addedBy = addedBy,
        createdAt = createdAt,
        authorDisplay = authorDisplay,
    )
}

fun GroceryGroup.toUi(): GroceryGroupUi =
    GroceryGroupUi(
        name = name,
        items = items.map { it.toUi() }.toImmutableList(),
    )
