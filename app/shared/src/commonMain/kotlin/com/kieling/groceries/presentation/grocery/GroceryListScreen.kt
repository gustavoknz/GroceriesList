package com.kieling.groceries.presentation.grocery

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import grocerieslist.app.shared.generated.resources.empty_list_subtitle
import grocerieslist.app.shared.generated.resources.empty_list_title
import grocerieslist.app.shared.generated.resources.group_name_label
import grocerieslist.app.shared.generated.resources.item_name_label
import grocerieslist.app.shared.generated.resources.rename
import grocerieslist.app.shared.generated.resources.rename_group_title
import grocerieslist.app.shared.generated.resources.rename_item_title
import grocerieslist.app.shared.generated.resources.save
import grocerieslist.app.shared.generated.resources.sign_out
import grocerieslist.app.shared.generated.resources.undo
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
                    title = {
                        val userName =
                            uiState.currentUser?.displayName?.substringBefore(" ")
                                ?: uiState.currentUser?.email?.substringBefore("@")
                        val titleText = if (!userName.isNullOrBlank()) "Olá, $userName!" else "Olá!"
                        Text(titleText)
                    },
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
                onDeleteItem = { groceryViewModel.deleteItem(it) },
                onDeleteGroup = { groceryViewModel.deleteGroup(it) },
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
fun CloseIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.error,
) {
    Canvas(modifier = modifier.size(14.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = Offset(0f, size.height),
            end = Offset(size.width, 0f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun GroceryListContent(
    uiState: GroceryListUiState,
    modifier: Modifier = Modifier,
    onUndoDelete: () -> Unit = {},
    onToggleItem: (Int) -> Unit = {},
    onDeleteItem: (Int) -> Unit = {},
    onDeleteGroup: (String) -> Unit = {},
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
        if (uiState.lastDeletedName != null) {
            Spacer(modifier = Modifier.height(4.dp))
            UndoBanner(
                deletedName = uiState.lastDeletedName,
                onUndo = onUndoDelete,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.items.isEmpty() && uiState.groups.isEmpty()) {
            EmptyGroceryState(modifier = Modifier.fillMaxWidth().weight(1f))
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                uiState.groups.forEach { group ->
                    item(key = "group_header_${group.name}") {
                        GroceryGroupHeader(
                            groupName = group.name,
                            itemCount = group.items.size,
                            onHeaderClick = { onStartRenameGroup(group.name) },
                            onRenameClick = { onStartRenameGroup(group.name) },
                            onDeleteGroup = { onDeleteGroup(group.name) },
                        )
                    }

                    items(group.items, key = { it.id }) { item ->
                        GroceryItemCard(
                            item = item,
                            onToggle = { onToggleItem(item.id) },
                            onItemClick = { onStartRenameItem(item) },
                            onDelete = { onDeleteItem(item.id) },
                        )
                    }

                    // Last item of each group: borderless placeholder to add an item
                    item(key = "add_item_to_${group.name}") {
                        var textValue by remember(group.name) { mutableStateOf("") }
                        TextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                            placeholder = {
                                Text(
                                    stringResource(Res.string.add_item_placeholder),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                )
                            },
                            singleLine = true,
                            colors =
                                TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
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

                // Last item in the list: borderless placeholder to add a group
                item(key = "add_group_placeholder") {
                    var groupText by remember { mutableStateOf("") }
                    TextField(
                        value = groupText,
                        onValueChange = { groupText = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                        placeholder = {
                            Text(
                                stringResource(Res.string.add_group_placeholder),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            )
                        },
                        singleLine = true,
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
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
    deletedName: String,
    onUndo: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Excluído \"$deletedName\"",
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
    onDeleteGroup: () -> Unit,
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
                .padding(top = 2.dp)
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                TextButton(onClick = onRenameClick) {
                    Text(stringResource(Res.string.rename), color = categoryContentColors[colorIndex])
                }
                IconButton(
                    onClick = onDeleteGroup,
                    modifier = Modifier.size(32.dp),
                ) {
                    CloseIcon(tint = categoryContentColors[colorIndex])
                }
            }
        }
    }
}

@Composable
fun GroceryItemCard(
    item: GroceryItemUi,
    onToggle: () -> Unit,
    onItemClick: () -> Unit,
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
                    .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { _ -> onToggle() },
                )
                Column(
                    modifier =
                        Modifier
                            .clickable(onClick = onItemClick)
                            .padding(vertical = 2.dp),
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
                    if (item.addedBy.isNotBlank()) {
                        Text(
                            text = stringResource(Res.string.added_by, item.addedBy),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp),
            ) {
                CloseIcon(tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
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
                                        GroceryItemUi(1, "Apples", false, "Produce", "Partner"),
                                        GroceryItemUi(2, "Bananas", true, "Produce", ""),
                                    ),
                            ),
                        ),
                ),
        )
    }
}

@Preview
@Composable
fun EmptyGroceryStatePreview() {
    MaterialTheme {
        EmptyGroceryState()
    }
}
