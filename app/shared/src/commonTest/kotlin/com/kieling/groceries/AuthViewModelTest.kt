package com.kieling.groceries

import com.kieling.groceries.domain.model.AuthUser
import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.SignInWithEmailUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignUpWithEmailUseCase
import com.kieling.groceries.presentation.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    var shouldFail = false

    override suspend fun signInWithGoogle(): Result<AuthUser> =
        if (shouldFail) {
            Result.failure(Exception("Google Sign-In failed"))
        } else {
            val user = AuthUser("google123", "google@example.com", "Google User", null)
            _currentUser.value = user
            Result.success(user)
        }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> =
        if (shouldFail) {
            Result.failure(Exception("Email Sign-In failed"))
        } else {
            val user = AuthUser("email123", email, "Email User", null)
            _currentUser.value = user
            Result.success(user)
        }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<AuthUser> =
        if (shouldFail) {
            Result.failure(Exception("Email Sign-Up failed"))
        } else {
            val user = AuthUser("email123", email, "New User", null)
            _currentUser.value = user
            Result.success(user)
        }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}

class AuthViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepo = FakeAuthRepository()
        viewModel =
            AuthViewModel(
                signInWithEmailUseCase = SignInWithEmailUseCase(authRepo),
                signUpWithEmailUseCase = SignUpWithEmailUseCase(authRepo),
                signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepo),
                observeCurrentUserUseCase = ObserveCurrentUserUseCase(authRepo),
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isRegistering)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun testUpdateEmailAndPassword() {
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        val state = viewModel.uiState.value
        assertEquals("test@example.com", state.email)
        assertEquals("password123", state.password)
    }

    @Test
    fun testToggleIsRegistering() {
        assertFalse(viewModel.uiState.value.isRegistering)
        viewModel.toggleIsRegistering()
        assertTrue(viewModel.uiState.value.isRegistering)
        viewModel.toggleIsRegistering()
        assertFalse(viewModel.uiState.value.isRegistering)
    }

    @Test
    fun testClearError() {
        viewModel.updateEmail("")
        viewModel.authenticate()
        assertNotNull(viewModel.uiState.value.errorMessage)
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testAuthenticateEmptyFields() {
        viewModel.authenticate()
        assertEquals("Please enter email and password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testAuthenticateSuccessSignIn() =
        runTest {
            viewModel.updateEmail("user@example.com")
            viewModel.updatePassword("password")
            var successCalled = false
            viewModel.authenticate { successCalled = true }
            assertTrue(successCalled)
            assertNotNull(viewModel.currentUser.value)
        }

    @Test
    fun testAuthenticateSuccessRegister() =
        runTest {
            viewModel.toggleIsRegistering()
            viewModel.updateEmail("newuser@example.com")
            viewModel.updatePassword("password")
            var successCalled = false
            viewModel.authenticate { successCalled = true }
            assertTrue(successCalled)
            assertNotNull(viewModel.currentUser.value)
        }

    @Test
    fun testAuthenticateFailure() =
        runTest {
            authRepo.shouldFail = true
            viewModel.updateEmail("user@example.com")
            viewModel.updatePassword("password")
            viewModel.authenticate()
            assertNotNull(viewModel.uiState.value.errorMessage)
        }

    @Test
    fun testSignInWithGoogle() =
        runTest {
            var successCalled = false
            viewModel.signInWithGoogle { successCalled = true }
            assertTrue(successCalled)
            assertNotNull(viewModel.currentUser.value)
        }

    @Test
    fun testSignInWithGoogleFailure() =
        runTest {
            authRepo.shouldFail = true
            var successCalled = false
            viewModel.signInWithGoogle { successCalled = true }
            assertFalse(successCalled)
            assertNotNull(viewModel.uiState.value.errorMessage)
        }
}
