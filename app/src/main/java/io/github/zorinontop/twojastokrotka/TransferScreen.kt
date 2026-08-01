package io.github.zorinontop.twojastokrotka

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(accessToken: String, onBack: () -> Unit, onNavigateToHistory: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Przekaż Płatki", "Przekaż znaczki")

    var balance by remember { mutableIntStateOf(0) }
    var transferValue by remember { mutableIntStateOf(1) }
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = apiService.getWalletDetails("Bearer $accessToken")
            balance = response.data?.balance?.toInt() ?: 0
        } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PRZEKAZY", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState())) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = StokrotkaGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = StokrotkaGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, fontSize = 14.sp) }
                    )
                }
            }

            if (selectedTabIndex == 1) {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Text("Dostępne w następnej aktualizacji", color = Color.White, fontWeight = FontWeight.Medium)
                }
            } else {
                Surface(modifier = Modifier.fillMaxWidth().height(60.dp), color = Color(0xFFFFD700)) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Twoje PŁATKI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(text = balance.toString(), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Ile przekazać", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (transferValue > 1) transferValue-- }) {
                                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                                }
                                Text(text = transferValue.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp), color = Color.White)
                                IconButton(onClick = { if (transferValue < balance) transferValue++ }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                }
                            }
                            Text(text = "Saldo po transakcji ${balance - transferValue}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(text = "Do kogo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Wybierz z listy kontaktów lub podaj numer telefonu", fontSize = 14.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(onClick = { /* Contacts */ }, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color.White)) {
                        Text("WYBIERZ", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Nazwa") },
                        placeholder = { Text("np. Tata") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent, 
                            focusedContainerColor = Color.Transparent,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = StokrotkaGreen,
                            cursorColor = StokrotkaGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = recipientPhone,
                        onValueChange = { if (it.length <= 9) recipientPhone = it },
                        label = { Text("Telefon") },
                        prefix = { Text("+48 ") },
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
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                try {
                                    val response = apiService.transferPetals("Bearer $accessToken", recipientName, recipientPhone, transferValue)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Przekazano pomyślnie!", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } else {
                                        Toast.makeText(context, "Błąd: ${response.code()}", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Błąd połączenia", Toast.LENGTH_LONG).show()
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
                        enabled = !isProcessing && recipientPhone.length == 9 && recipientName.isNotEmpty()
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("PRZEKAŻ $transferValue PŁATEK", fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text("HISTORIA PRZEKAZÓW", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
