package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen

@Composable
fun ActionDialog(onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Wybierz akcję", 
                modifier = Modifier.fillMaxWidth(), 
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            ) 
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                ) {
                    Text("Edytuj", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                ) {
                    Text("Usuń", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF1C1B1F),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@Composable
fun RenameDialog(initialName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zmień nazwę", fontWeight = FontWeight.Bold, color = Color.White) },
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
                onClick = { if (name.isNotEmpty()) onSave(name) },
                colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
                shape = RoundedCornerShape(4.dp),
                enabled = name.isNotEmpty()
            ) {
                Text("ZAPISZ", color = Color.White)
            }
        },
        containerColor = Color(0xFF1C1B1F),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
