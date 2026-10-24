package com.kieling.groceries

import androidx.compose.runtime.Immutable

@Immutable
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)
