package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.local.LocalShoppingList
import io.github.zorinontop.twojastokrotka.local.ShoppingManager
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListsScreen(accessToken: String, onBack: () -> Unit, onListClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val manager = remember { ShoppingManager(context) }
    var lists by remember { mutableStateOf(manager.getLists()) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedListForAction by remember { mutableStateOf<LocalShoppingList?>(null) }
    var showRenameDialog by remember { mutableStateOf<LocalShoppingList?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LISTY ZAKUPOWE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (lists.isEmpty()) {
                EmptyListsState(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(lists) { list ->
                        ShoppingListRow(
                            list = list, 
                            onClick = { onListClick(list.id, list.name) },
                            onLongClick = { selectedListForAction = list }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
                
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .height(48.dp)
                        .width(200.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("UTWÓRZ LISTĘ", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddDialog) {
        NewListDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name ->
                manager.addList(name)
                lists = manager.getLists()
                showAddDialog = false
            }
        )
    }

    if (selectedListForAction != null) {
        ActionDialog(
            onDismiss = { selectedListForAction = null },
            onEdit = { 
                showRenameDialog = selectedListForAction
                selectedListForAction = null
            },
            onDelete = {
                manager.deleteList(selectedListForAction!!.id)
                lists = manager.getLists()
                selectedListForAction = null
            }
        )
    }

    if (showRenameDialog != null) {
        RenameDialog(
            initialName = showRenameDialog!!.name,
            onDismiss = { showRenameDialog = null },
            onSave = { newName ->
                manager.renameList(showRenameDialog!!.id, newName)
                lists = manager.getLists()
                showRenameDialog = null
            }
        )
    }
}

@Composable
fun EmptyListsState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(100.dp), tint = StokrotkaGreen.copy(alpha = 0.1f))
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp).background(StokrotkaGreen, androidx.compose.foundation.shape.CircleShape).padding(8.dp), tint = Color.White)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Utwórz listę i dodaj pierwszy produkt",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Twórz listy ulubionych produktów i miej wszystko pod ręką!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onAddClick,
            modifier = Modifier.height(48.dp).width(200.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("UTWÓRZ LISTĘ", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListRow(list: LocalShoppingList, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = list.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "(${list.items.size})",
            color = StokrotkaGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NewListDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowa lista", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { if (it.length <= 32) name = it },
                    label = { Text("Nazwa", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        focusedLabelColor = StokrotkaGreen,
                        cursorColor = StokrotkaGreen
                    ),
                    singleLine = true
                )
                Text(
                    text = "${name.length}/32",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty()) onAdd(name) },
                colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
                shape = RoundedCornerShape(4.dp),
                enabled = name.isNotEmpty()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("DODAJ", color = Color.White)
            }
        },
        containerColor = Color(0xFF1C1B1F),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
