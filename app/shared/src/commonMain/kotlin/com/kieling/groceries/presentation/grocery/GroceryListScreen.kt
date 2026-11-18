package com.kieling.groceries.presentation.grocery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kieling.groceries.Logger
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
    var groupBeingRenamed by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    if (groupBeingRenamed != null) {
        AlertDialog(
            onDismissRequest = { groupBeingRenamed = null },
            title = { Text("Rename Group") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        groupBeingRenamed?.let { oldName ->
                            groceryViewModel.renameGroup(oldName, renameText)
                        }
                        groupBeingRenamed = null
                    },
                    enabled = renameText.isNotBlank(),
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { groupBeingRenamed = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (uiState.currentUser == null) {
        LoginScreen(
            authViewModel = authViewModel,
            onSignInClick = {
                Logger.i("GroceryListScreen", "Sign in with Google button clicked")
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

                // Input card to add new grocery item
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = uiState.newItemName,
                            onValueChange = { groceryViewModel.updateNewItemName(it) },
                            modifier = Modifier.weight(1.3f),
                            placeholder = { Text("Item name...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        groceryViewModel.addItem()
                                    },
                                ),
                        )
                        OutlinedTextField(
                            value = uiState.newItemGroup,
                            onValueChange = { groceryViewModel.updateNewItemGroup(it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Group (optional)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        groceryViewModel.addItem()
                                    },
                                ),
                        )
                        Button(
                            onClick = {
                                groceryViewModel.addItem()
                            },
                        ) {
                            Text("Add")
                        }
                    }
                }

                if (uiState.lastDeletedItemName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Deleted \"${uiState.lastDeletedItemName}\"",
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                            )
                            TextButton(onClick = { groceryViewModel.undoDelete() }) {
                                Text("Undo", color = MaterialTheme.colorScheme.inversePrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grocery Items List Grouped by Group
                if (uiState.items.isEmpty()) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "🛒",
                            style = MaterialTheme.typography.displayMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your grocery list is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add items above to shop together with your partner!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        uiState.groups.forEach { group ->
                            item(key = "group_header_${group.name}") {
                                val categoryColors =
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                val categoryContentColors =
                                    listOf(
                                        MaterialTheme.colorScheme.onPrimaryContainer,
                                        MaterialTheme.colorScheme.onSecondaryContainer,
                                        MaterialTheme.colorScheme.onTertiaryContainer,
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                val colorIndex = kotlin.math.abs(group.name.hashCode()) % categoryColors.size
                                Surface(
                                    color = categoryColors[colorIndex],
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                                ) {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = group.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = categoryContentColors[colorIndex],
                                            )
                                            Text(
                                                text = "(${group.items.size})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = categoryContentColors[colorIndex].copy(alpha = 0.7f),
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                groupBeingRenamed = group.name
                                                renameText = group.name
                                            },
                                        ) {
                                            Text("Rename", color = categoryContentColors[colorIndex])
                                        }
                                    }
                                }
                            }

                            items(group.items, key = { it.id }) { item ->
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
                                                .padding(horizontal = 16.dp, vertical = 4.dp),
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
}
