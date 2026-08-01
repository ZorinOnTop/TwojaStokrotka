package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.local.LocalShoppingItem
import io.github.zorinontop.twojastokrotka.local.ShoppingManager
import io.github.zorinontop.twojastokrotka.models.getIconForCategory
import io.github.zorinontop.twojastokrotka.models.shoppingCategories
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListDetailScreen(listId: String, listName: String, accessToken: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val manager = remember { ShoppingManager(context) }
    var items by remember { mutableStateOf(manager.getLists().find { it.id == listId }?.items ?: emptyList()) }
    
    var showAddProduct by remember { mutableStateOf(false) }
    var selectedItemForAction by remember { mutableStateOf<LocalShoppingItem?>(null) }
    var showRenameItemDialog by remember { mutableStateOf<LocalShoppingItem?>(null) }

    fun refreshItems() {
        items = manager.getLists().find { it.id == listId }?.items?.toList() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
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
            if (items.isEmpty()) {
                EmptyProductsState(listName, onAddClick = { showAddProduct = true })
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { product ->
                        ProductRow(
                            product = product,
                            onCheckedChange = { checked ->
                                manager.toggleItem(listId, product.id, checked)
                                refreshItems()
                            },
                            onLongClick = { selectedItemForAction = product }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
                
                Button(
                    onClick = { showAddProduct = true },
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
                    Text("DODAJ PRODUKT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddProduct) {
        NewProductDialog(
            onDismiss = { showAddProduct = false },
            onAdd = { name, category ->
                manager.addItem(listId, name, category)
                refreshItems()
                showAddProduct = false
            }
        )
    }

    if (selectedItemForAction != null) {
        ActionDialog(
            onDismiss = { selectedItemForAction = null },
            onEdit = {
                showRenameItemDialog = selectedItemForAction
                selectedItemForAction = null
            },
            onDelete = {
                manager.deleteItem(listId, selectedItemForAction!!.id)
                refreshItems()
                selectedItemForAction = null
            }
        )
    }

    if (showRenameItemDialog != null) {
        RenameDialog(
            initialName = showRenameItemDialog!!.name,
            onDismiss = { showRenameItemDialog = null },
            onSave = { newName ->
                manager.renameItem(listId, showRenameItemDialog!!.id, newName)
                refreshItems()
                showRenameItemDialog = null
            }
        )
    }
}

@Composable
fun EmptyProductsState(listName: String, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null, modifier = Modifier.size(100.dp), tint = StokrotkaGreen.copy(alpha = 0.1f))
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp).background(StokrotkaGreen, androidx.compose.foundation.shape.CircleShape).padding(8.dp), tint = Color.White)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Nie masz dodanych produktów do listy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Dodawaj produkty w stworzonych przez siebie listach!",
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
            Text("DODAJ PRODUKT", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductRow(product: LocalShoppingItem, onCheckedChange: (Boolean) -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onCheckedChange(!product.isChecked) },
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = product.isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = StokrotkaGreen)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (product.isChecked) Color.Gray else Color.White,
            textDecoration = if (product.isChecked) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = getIconForCategory(product.categoryName),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProductDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Inne") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    if (showCategoryPicker) {
        CategoryPickerSheet(
            onDismiss = { showCategoryPicker = false },
            onSelect = { 
                selectedCategory = it
                showCategoryPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy produkt", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { if (it.length <= 40) name = it },
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
                    text = "${name.length}/40",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryPicker = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = getIconForCategory(selectedCategory), contentDescription = null, tint = StokrotkaGreen)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Wybierz kategorię (opcjonalne)", fontSize = 12.sp, color = Color.Gray)
                        Text(selectedCategory, fontSize = 16.sp, color = Color.White)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty()) onAdd(name, selectedCategory) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerSheet(onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1B1F),
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                text = "Kategorie",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(shoppingCategories) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(category.name) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category.name, modifier = Modifier.weight(1f), fontSize = 16.sp, color = Color.White)
                        Icon(imageVector = category.icon, contentDescription = null, tint = StokrotkaGreen)
                    }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                }
            }
        }
    }
}
