package com.kieling.groceries.di

import com.kieling.groceries.data.repository.AuthRepositoryImpl
import com.kieling.groceries.data.repository.GroceryRepositoryImpl
import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.domain.repository.GroceryRepository
import com.kieling.groceries.domain.usecase.AddGroceryItemUseCase
import com.kieling.groceries.domain.usecase.DeleteGroceryItemUseCase
import com.kieling.groceries.domain.usecase.GetGroceryItemsUseCase
import com.kieling.groceries.domain.usecase.ObserveCurrentUserUseCase
import com.kieling.groceries.domain.usecase.SignInWithEmailUseCase
import com.kieling.groceries.domain.usecase.SignInWithGoogleUseCase
import com.kieling.groceries.domain.usecase.SignOutUseCase
import com.kieling.groceries.domain.usecase.SignUpWithEmailUseCase
import com.kieling.groceries.domain.usecase.ToggleGroceryItemUseCase
import com.kieling.groceries.presentation.auth.AuthViewModel
import com.kieling.groceries.presentation.grocery.GroceryListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single {
            HttpClient {
                install(ContentNegotiation) {
                    json()
                }
            }
        }
        single<AuthRepository> { AuthRepositoryImpl() }
        single<GroceryRepository> { GroceryRepositoryImpl(get()) }

        factory { ObserveCurrentUserUseCase(get()) }
        factory { SignInWithGoogleUseCase(get()) }
        factory { SignInWithEmailUseCase(get()) }
        factory { SignUpWithEmailUseCase(get()) }
        factory { SignOutUseCase(get()) }
        factory { GetGroceryItemsUseCase(get()) }
        factory { AddGroceryItemUseCase(get()) }
        factory { ToggleGroceryItemUseCase(get()) }
        factory { DeleteGroceryItemUseCase(get()) }

        viewModel { AuthViewModel(get(), get(), get(), get()) }
        viewModel { GroceryListViewModel(get(), get(), get(), get(), get(), get(), get()) }
    }
