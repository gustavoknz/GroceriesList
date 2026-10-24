package com.kieling.groceries.presentation.grocery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kieling.groceries.presentation.auth.AuthViewModel
import com.kieling.groceries.presentation.auth.LoginScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GroceryListScreen(
    groceryViewModel: GroceryListViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by groceryViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    if (uiState.currentUser == null) {
        LoginScreen(
            authViewModel = authViewModel,
            onSignInClick = {
                scope.launch {
                    authViewModel.signInWithGoogle()
                }
            },
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Groceries List") },
                    actions = {
                        TextButton(
                            onClick = {
                                groceryViewModel.signOut()
                            },
                        ) {
                            Text("Sign Out", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                )
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Welcome message showing user's name
                Text(
                    text = "Welcome, ${uiState.currentUser?.displayName ?: uiState.currentUser?.email ?: "User"}!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                )

                // Input row to add new grocery item
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.newItemName,
                        onValueChange = { groceryViewModel.updateNewItemName(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add new grocery item...") },
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            groceryViewModel.addItem()
                        },
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grocery Items List
                if (uiState.items.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Your grocery list is empty!",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            groceryViewModel.toggleItem(item.id)
                                        },
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            if (item.isChecked) {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            },
                                    ),
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Checkbox(
                                            checked = item.isChecked,
                                            onCheckedChange = { _ ->
                                                groceryViewModel.toggleItem(item.id)
                                            },
                                        )
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                            color =
                                                if (item.isChecked) {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            groceryViewModel.deleteItem(item.id)
                                        },
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
