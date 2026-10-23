package com.kieling.groceries.di

import com.kieling.groceries.data.repository.AuthRepositoryImpl
import com.kieling.groceries.data.repository.GroceryRepositoryImpl
import com.kieling.groceries.domain.repository.AuthRepository
import com.kieling.groceries.domain.repository.GroceryRepository
import com.kieling.groceries.domain.usecase.*
import com.kieling.groceries.presentation.auth.AuthViewModel
import com.kieling.groceries.presentation.grocery.GroceryListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single<AuthRepository> { AuthRepositoryImpl() }
        single<GroceryRepository> { GroceryRepositoryImpl() }

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
