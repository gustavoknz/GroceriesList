package com.kieling.groceries

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
import kotlinx.coroutines.launch

@Composable
fun GroceryListScreen() {
    val authManager = remember { AuthManager() }
    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    if (currentUser == null) {
        LoginScreen(
            onSignInClick = {
                scope.launch {
                    authManager.signInWithGoogle()
                }
            },
        )
    } else {
        var items by remember {
            mutableStateOf(
                listOf(
                    GroceryItem(1, "Apples"),
                    GroceryItem(2, "Milk"),
                    GroceryItem(3, "Bread"),
                    GroceryItem(4, "Eggs"),
                    GroceryItem(5, "Cheese"),
                    GroceryItem(6, "Coffee"),
                ),
            )
        }
        var newItemName by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Groceries List") },
                    actions = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    authManager.signOut()
                                }
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
                    text = "Welcome, ${currentUser?.displayName ?: currentUser?.email ?: "User"}!",
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
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add new grocery item...") },
                        singleLine = true,
                    )
                    Button(
                        onClick = {
                            if (newItemName.isNotBlank()) {
                                val newId = (items.maxOfOrNull { it.id } ?: 0) + 1
                                items = items + GroceryItem(id = newId, name = newItemName.trim())
                                newItemName = ""
                            }
                        },
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grocery Items List
                if (items.isEmpty()) {
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
                        items(items, key = { it.id }) { item ->
                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            items =
                                                items.map {
                                                    if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                                                }
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
                                            onCheckedChange = { checked ->
                                                items =
                                                    items.map {
                                                        if (it.id == item.id) item.copy(isChecked = checked) else it
                                                    }
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
                                            items = items.filter { it.id != item.id }
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
