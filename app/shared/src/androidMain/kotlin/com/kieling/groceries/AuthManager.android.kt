package com.kieling.groceries

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object AndroidContextHolder {
    var applicationContext: Context? = null
}

object CurrentActivityHolder {
    var currentActivity: Activity? = null
}

actual class AuthManager actual constructor() {
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _currentUser =
        MutableStateFlow<AuthUser?>(
            try {
                firebaseAuth?.currentUser?.let {
                    AuthUser(it.uid, it.email, it.displayName, it.photoUrl?.toString())
                }
            } catch (e: Exception) {
                null
            },
        )
    actual val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                _currentUser.value =
                    user?.let {
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
            val activity =
                CurrentActivityHolder.currentActivity
                    ?: return Result.failure(Exception("Active Activity not available. Please try again."))

            val credentialManager = CredentialManager.create(activity)
            val googleIdOption =
                GetGoogleIdOption.Builder()
                    .setServerClientId("1017859145345-0d0r9d12ec1c52155ef89b5780.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()

            val request =
                GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                if (user != null) {
                    val authUser = AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                    _currentUser.value = authUser
                    Result.success(authUser)
                } else {
                    Result.failure(Exception("Firebase sign-in failed: user is null"))
                }
            } else {
                Result.failure(Exception("Invalid credential type returned from Google Sign-In"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> {
        return try {
            val auth = firebaseAuth ?: return Result.failure(Exception("Firebase is not initialized."))
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val authUser = AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                _currentUser.value = authUser
                Result.success(authUser)
            } else {
                Result.failure(Exception("Email sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> {
        return try {
            val auth = firebaseAuth ?: return Result.failure(Exception("Firebase is not initialized."))
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val authUser = AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                _currentUser.value = authUser
                Result.success(authUser)
            } else {
                Result.failure(Exception("Email sign-up failed"))
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
