package com.kieling.groceries.data

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kieling.groceries.Logger
import com.kieling.groceries.domain.model.AuthUser
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
    private val tag = "AuthManager"

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
            Logger.e(tag, "Error adding auth state listener", e)
        }
    }

    actual suspend fun signInWithGoogle(): Result<AuthUser> {
        Logger.i(tag, "Attempting Google Sign-In...")
        return try {
            val auth =
                firebaseAuth ?: run {
                    val err = Exception("Firebase is not initialized. Please add google-services.json.")
                    Logger.e(tag, "Google Sign-In failed: Firebase not initialized", err)
                    return Result.failure(err)
                }
            val activity =
                CurrentActivityHolder.currentActivity
                    ?: run {
                        val err = Exception("Active Activity not available. Please try again.")
                        Logger.e(tag, "Google Sign-In failed: Active Activity not available", err)
                        return Result.failure(err)
                    }

            val clientId =
                try {
                    val resId = activity.resources.getIdentifier("google_client_id", "string", activity.packageName)
                    if (resId != 0) {
                        activity.getString(resId)
                    } else {
                        throw IllegalStateException("google_client_id resource not found")
                    }
                } catch (e: Exception) {
                    val err = Exception("Google Client ID not configured: ${e.message}")
                    Logger.e(tag, "Google Sign-In failed: Client ID missing", err)
                    return Result.failure(err)
                }

            val credentialManager = CredentialManager.create(activity)
            val googleIdOption =
                GetGoogleIdOption
                    .Builder()
                    .setServerClientId(clientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()

            val request =
                GetCredentialRequest
                    .Builder()
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
                    Logger.i(tag, "Google Sign-In successful for user: ${authUser.email} (${authUser.uid})")
                    Result.success(authUser)
                } else {
                    val err = Exception("Firebase sign-in failed: user is null")
                    Logger.e(tag, "Google Sign-In failed", err)
                    Result.failure(err)
                }
            } else {
                val err = Exception("Invalid credential type returned from Google Sign-In")
                Logger.e(tag, "Google Sign-In failed", err)
                Result.failure(err)
            }
        } catch (e: Exception) {
            Logger.e(tag, "Google Sign-In threw exception", e)
            Result.failure(e)
        }
    }

    actual suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> {
        Logger.i(tag, "Attempting Email Sign-In for email: $email")
        return try {
            val auth =
                firebaseAuth ?: run {
                    val err = Exception("Firebase is not initialized.")
                    Logger.e(tag, "Email Sign-In failed: Firebase not initialized", err)
                    return Result.failure(err)
                }
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val authUser = AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                _currentUser.value = authUser
                Logger.i(tag, "Email Sign-In successful for user: ${authUser.email} (${authUser.uid})")
                Result.success(authUser)
            } else {
                val err = Exception("Email sign-in failed")
                Logger.e(tag, "Email Sign-In failed: user is null", err)
                Result.failure(err)
            }
        } catch (e: Exception) {
            Logger.e(tag, "Email Sign-In threw exception for email: $email", e)
            Result.failure(e)
        }
    }

    actual suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> {
        Logger.i(tag, "Attempting Email Sign-Up for email: $email")
        return try {
            val auth =
                firebaseAuth ?: run {
                    val err = Exception("Firebase is not initialized.")
                    Logger.e(tag, "Email Sign-Up failed: Firebase not initialized", err)
                    return Result.failure(err)
                }
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val authUser = AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                _currentUser.value = authUser
                Logger.i(tag, "Email Sign-Up successful for user: ${authUser.email} (${authUser.uid})")
                Result.success(authUser)
            } else {
                val err = Exception("Email sign-up failed")
                Logger.e(tag, "Email Sign-Up failed: user is null", err)
                Result.failure(err)
            }
        } catch (e: Exception) {
            Logger.e(tag, "Email Sign-Up threw exception for email: $email", e)
            Result.failure(e)
        }
    }

    actual suspend fun signOut() {
        Logger.i(tag, "Signing out user...")
        try {
            firebaseAuth?.signOut()
            Logger.i(tag, "Sign-out successful")
        } catch (e: Exception) {
            Logger.e(tag, "Error during sign-out", e)
        }
        _currentUser.value = null
    }
}
