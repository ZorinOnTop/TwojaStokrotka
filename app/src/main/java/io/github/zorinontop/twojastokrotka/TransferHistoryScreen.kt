package io.github.zorinontop.twojastokrotka

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.network.TransferElement
import io.github.zorinontop.twojastokrotka.network.TransferHistoryRequest
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferHistoryScreen(accessToken: String, onBack: () -> Unit, onNavigateToTransfer: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }

    var balance by remember { mutableStateOf("...") }
    var history by remember { mutableStateOf<List<TransferElement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val refreshData = suspend {
        try {
            val bearerToken = "Bearer $accessToken"
            val walletResponse = apiService.getWalletDetails(bearerToken)
            balance = walletResponse.data?.balance?.toInt()?.toString() ?: "0"

            val historyResponse = apiService.getTransferHistory(bearerToken, TransferHistoryRequest(skip = 0, take = 20))
            history = historyResponse.data?.elements ?: emptyList()
        } catch (e: Exception) {}
    }

    LaunchedEffect(Unit) {
        refreshData()
        isLoading = false
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
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth().height(80.dp), tonalElevation = 8.dp) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = onNavigateToTransfer,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                    ) {
                        Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("PRZEKAŻ PŁATKI", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Surface(modifier = Modifier.fillMaxWidth().height(50.dp), color = Color(0xFFFFD700)) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Twoje PŁATKI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(text = balance, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Surface(modifier = Modifier.fillMaxWidth().height(50.dp), color = Color(0xFFFFD700)) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Twoje ZNACZKI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(text = "0", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Historia przekazów", fontWeight = FontWeight.Black, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 16.dp), color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StokrotkaGreen)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { entry ->
                        TransferHistoryItem(entry, onCancel = {
                            coroutineScope.launch {
                                try {
                                    val bearerToken = "Bearer $accessToken"
                                    val response = apiService.cancelTransfer(bearerToken, entry.transferId)
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Anulowano", Toast.LENGTH_SHORT).show()
                                        refreshData()
                                    }
                                } catch (e: Exception) {}
                            }
                        })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun TransferHistoryItem(entry: TransferElement, onCancel: () -> Unit) {
    val pointsColor = if (entry.points > 0) StokrotkaGreen else Color(0xFFE57373)
    val isCancelled = entry.status.contains("Cancelled", ignoreCase = true)
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.comment ?: "Anonim", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text(text = entry.phoneNumber, color = Color.LightGray, fontSize = 12.sp)
            Text(text = "${entry.status} ${formatOperationDate(entry.transferDate)}", fontSize = 11.sp, color = Color.LightGray)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${if (entry.points > 0) "+" else ""}${entry.points}",
                color = pointsColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = if (Math.abs(entry.points) == 1) " Płatek" else " Płatków",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isCancelled) Icons.Default.Close else Icons.Default.Eco,
                contentDescription = null,
                tint = if (isCancelled) Color.Gray else pointsColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Surface(
            modifier = Modifier.size(36.dp).clickable { onCancel() },
            shape = RoundedCornerShape(4.dp),
            color = StokrotkaGreen
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Repeat/Cancel",
                tint = Color.White,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
