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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kieling.groceries.Logger
import com.kieling.groceries.presentation.auth.AuthViewModel
import com.kieling.groceries.presentation.auth.LoginScreen
import com.kieling.groceries.presentation.model.GroceryGroupUi
import com.kieling.groceries.presentation.model.GroceryItemUi
import grocerieslist.app.shared.generated.resources.Res
import grocerieslist.app.shared.generated.resources.add_group_placeholder
import grocerieslist.app.shared.generated.resources.add_item_placeholder
import grocerieslist.app.shared.generated.resources.added_by
import grocerieslist.app.shared.generated.resources.cancel
import grocerieslist.app.shared.generated.resources.delete
import grocerieslist.app.shared.generated.resources.deleted_item
import grocerieslist.app.shared.generated.resources.empty_list_subtitle
import grocerieslist.app.shared.generated.resources.empty_list_title
import grocerieslist.app.shared.generated.resources.groceries_list
import grocerieslist.app.shared.generated.resources.group_name_label
import grocerieslist.app.shared.generated.resources.item_name_label
import grocerieslist.app.shared.generated.resources.rename
import grocerieslist.app.shared.generated.resources.rename_group_title
import grocerieslist.app.shared.generated.resources.rename_item_title
import grocerieslist.app.shared.generated.resources.save
import grocerieslist.app.shared.generated.resources.sign_out
import grocerieslist.app.shared.generated.resources.undo
import grocerieslist.app.shared.generated.resources.welcome_user
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GroceryListScreen(
    groceryViewModel: GroceryListViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by groceryViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var groupBeingRenamed by remember { mutableStateOf<String?>(null) }
    var itemBeingRenamed by remember { mutableStateOf<GroceryItemUi?>(null) }
    var renameText by remember { mutableStateOf("") }

    if (groupBeingRenamed != null) {
        RenameGroupDialog(
            groupName = groupBeingRenamed!!,
            initialRenameText = renameText,
            onDismiss = { groupBeingRenamed = null },
            onSave = { newName ->
                groupBeingRenamed?.let { oldName ->
                    groceryViewModel.renameGroup(oldName, newName)
                }
                groupBeingRenamed = null
            },
        )
    }

    if (itemBeingRenamed != null) {
        RenameItemDialog(
            itemName = itemBeingRenamed!!.name,
            initialRenameText = itemBeingRenamed!!.name,
            onDismiss = { itemBeingRenamed = null },
            onSave = { newName ->
                itemBeingRenamed?.let { item ->
                    groceryViewModel.renameItem(item.id, newName)
                }
                itemBeingRenamed = null
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
                    title = { Text(stringResource(Res.string.groceries_list)) },
                    actions = {
                        TextButton(
                            onClick = { groceryViewModel.signOut() },
                        ) {
                            Text(stringResource(Res.string.sign_out), color = MaterialTheme.colorScheme.error)
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
            GroceryListContent(
                uiState = uiState,
                modifier = Modifier.padding(paddingValues),
                onUndoDelete = { groceryViewModel.undoDelete() },
                onToggleItem = { groceryViewModel.toggleItem(it) },
                onIncrement = { groceryViewModel.incrementQuantity(it) },
                onDecrement = { groceryViewModel.decrementQuantity(it) },
                onDelete = { groceryViewModel.deleteItem(it) },
                onStartRenameGroup = { groupName ->
                    groupBeingRenamed = groupName
                    renameText = groupName
                },
                onStartRenameItem = { item ->
                    itemBeingRenamed = item
                },
                onAddItemToGroup = { groupName, itemName ->
                    groceryViewModel.addItemToGroup(groupName, itemName)
                },
                onAddGroup = { groupName ->
                    groceryViewModel.addCategory(groupName)
                },
            )
        }
    }
}

@Composable
fun GroceryListContent(
    uiState: GroceryListUiState,
    modifier: Modifier = Modifier,
    onUndoDelete: () -> Unit = {},
    onToggleItem: (Int) -> Unit = {},
    onIncrement: (Int) -> Unit = {},
    onDecrement: (Int) -> Unit = {},
    onDelete: (Int) -> Unit = {},
    onStartRenameGroup: (String) -> Unit = {},
    onStartRenameItem: (GroceryItemUi) -> Unit = {},
    onAddItemToGroup: (String, String) -> Unit = { _, _ -> },
    onAddGroup: (String) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val userName = uiState.currentUser?.displayName ?: uiState.currentUser?.email ?: "Usuário"
        Text(
            text = stringResource(Res.string.welcome_user, userName),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
        )

        if (uiState.lastDeletedItemName != null) {
            Spacer(modifier = Modifier.height(8.dp))
            UndoBanner(
                deletedItemName = uiState.lastDeletedItemName,
                onUndo = onUndoDelete,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.items.isEmpty() && uiState.groups.isEmpty()) {
            EmptyGroceryState(modifier = Modifier.fillMaxWidth().weight(1f))
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
                        GroceryGroupHeader(
                            groupName = group.name,
                            itemCount = group.items.size,
                            onHeaderClick = { onStartRenameGroup(group.name) },
                            onRenameClick = { onStartRenameGroup(group.name) },
                        )
                    }

                    items(group.items, key = { it.id }) { item ->
                        GroceryItemCard(
                            item = item,
                            onToggle = { onToggleItem(item.id) },
                            onItemClick = { onStartRenameItem(item) },
                            onIncrement = { onIncrement(item.id) },
                            onDecrement = { onDecrement(item.id) },
                            onDelete = { onDelete(item.id) },
                        )
                    }

                    // Last item of each group: placeholder to add an item
                    item(key = "add_item_to_${group.name}") {
                        var textValue by remember(group.name) { mutableStateOf("") }
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            placeholder = { Text(stringResource(Res.string.add_item_placeholder)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        if (textValue.isNotBlank()) {
                                            onAddItemToGroup(group.name, textValue)
                                            textValue = ""
                                        }
                                    },
                                ),
                        )
                    }
                }

                // Last item in the list: placeholder to add a group
                item(key = "add_group_placeholder") {
                    var groupText by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = groupText,
                        onValueChange = { groupText = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                        placeholder = { Text(stringResource(Res.string.add_group_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    if (groupText.isNotBlank()) {
                                        onAddGroup(groupText)
                                        groupText = ""
                                    }
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun UndoBanner(
    deletedItemName: String,
    onUndo: () -> Unit,
) {
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
                text = stringResource(Res.string.deleted_item, deletedItemName),
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
            TextButton(onClick = onUndo) {
                Text(stringResource(Res.string.undo), color = MaterialTheme.colorScheme.inversePrimary)
            }
        }
    }
}

@Composable
fun EmptyGroceryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🛒",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.empty_list_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.empty_list_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun GroceryGroupHeader(
    groupName: String,
    itemCount: Int,
    onHeaderClick: () -> Unit,
    onRenameClick: () -> Unit,
) {
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
    val colorIndex = kotlin.math.abs(groupName.hashCode()) % categoryColors.size

    Surface(
        color = categoryColors[colorIndex],
        shape = MaterialTheme.shapes.small,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 3.dp)
                .clickable(onClick = onHeaderClick),
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
                    text = groupName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryContentColors[colorIndex],
                )
                Text(
                    text = "($itemCount)",
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryContentColors[colorIndex].copy(alpha = 0.7f),
                )
            }

            TextButton(onClick = onRenameClick) {
                Text(stringResource(Res.string.rename), color = categoryContentColors[colorIndex])
            }
        }
    }
}

@Composable
fun GroceryItemCard(
    item: GroceryItemUi,
    onToggle: () -> Unit,
    onItemClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(0.68f),
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { _ -> onToggle() },
                )
                Column(
                    modifier =
                        Modifier
                            .clickable(onClick = onItemClick)
                            .padding(vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
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
                        if (item.quantity > 1) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.extraSmall,
                            ) {
                                Text(
                                    text = "${item.quantity}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                    if (item.addedBy.isNotBlank()) {
                        Text(
                            text = stringResource(Res.string.added_by, item.addedBy),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                TextButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(28.dp),
                ) {
                    Text("-", fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(28.dp),
                ) {
                    Text("+", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun RenameGroupDialog(
    groupName: String,
    initialRenameText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var renameText by remember { mutableStateOf(initialRenameText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(Res.string.rename_group_title)}: $groupName") },
        text = {
            OutlinedTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = { Text(stringResource(Res.string.group_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(renameText) },
                enabled = renameText.isNotBlank(),
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Composable
fun RenameItemDialog(
    itemName: String,
    initialRenameText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var renameText by remember { mutableStateOf(initialRenameText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(Res.string.rename_item_title)}: $itemName") },
        text = {
            OutlinedTextField(
                value = renameText,
                onValueChange = { renameText = it },
                label = { Text(stringResource(Res.string.item_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(renameText) },
                enabled = renameText.isNotBlank(),
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}

@Preview
@Composable
fun GroceryListContentPreview() {
    MaterialTheme {
        GroceryListContent(
            uiState =
                GroceryListUiState(
                    groups =
                        persistentListOf(
                            GroceryGroupUi(
                                name = "Produce",
                                items =
                                    persistentListOf(
                                        GroceryItemUi(1, "Apples", false, "Produce", 2, "Partner"),
                                        GroceryItemUi(2, "Bananas", true, "Produce", 1, ""),
                                    ),
                            ),
                        ),
                ),
        )
    }
}
