package com.kieling.groceries

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class AuthManager actual constructor() {
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUser = MutableStateFlow<AuthUser?>(
        try {
            firebaseAuth?.currentUser?.let {
                AuthUser(it.uid, it.email, it.displayName, it.photoUrl?.toString())
            }
        } catch (e: Exception) {
            null
        }
    )
    actual val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                _currentUser.value = user?.let {
                    AuthUser(it.uid, it.email, it.displayName, it.photoUrl?.toString())
                }
            }
        } catch (e: Exception) {
            // Ignored if Firebase is not yet configured
        }
    }

    actual suspend fun signInWithGoogle(): Result<AuthUser> {
        return try {
            val auth = firebaseAuth ?: return Result.failure(Exception("Firebase is not initialized. Please add google-services.json."))
            val user = auth.currentUser
            println("TEGUI user: $user")
            if (user != null) {
                Result.success(AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString()))
            } else {
                Result.failure(Exception("Please configure Google Sign-In credentials in Firebase Console."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // Ignored
        }
        _currentUser.value = null
    }
}
