package com.kieling.groceries.data.model

import com.kieling.groceries.domain.model.AuthUser
import kotlinx.serialization.Serializable

@Serializable
data class AuthUserDto(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)

fun AuthUserDto.toDomain(): AuthUser =
    AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
    )

fun AuthUser.toDto(): AuthUserDto =
    AuthUserDto(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
    )
