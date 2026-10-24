package com.kieling.groceries.model

import androidx.compose.runtime.Immutable

@Immutable
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)
