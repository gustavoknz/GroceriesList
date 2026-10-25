package com.kieling.groceries.presentation.model

import androidx.compose.runtime.Immutable
import com.kieling.groceries.domain.model.AuthUser

@Immutable
data class AuthUserUi(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)

fun AuthUser.toUi(): AuthUserUi =
    AuthUserUi(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
    )
