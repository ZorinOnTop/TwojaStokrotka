package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.network.HistoryEntry
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(accessToken: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }

    var balance by remember { mutableStateOf("...") }
    var history by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val walletResponse = apiService.getWalletDetails(bearerToken)
                balance = walletResponse.data?.balance?.toInt()?.toString() ?: "0"

                val historyResponse = apiService.getWalletHistory(bearerToken)
                history = historyResponse.data?.reversed() ?: emptyList()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HISTORIA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                color = Color(0xFFFFD700)
            ) {
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
                    Text(text = balance, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StokrotkaGreen)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { entry ->
                        HistoryItem(entry)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(entry: HistoryEntry) {
    val isPositive = entry.points > 0
    val pointsText = if (isPositive) "+${entry.points}" else "${entry.points}"
    val pointsColor = if (isPositive) StokrotkaGreen else Color(0xFFE57373)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pointsText,
                color = pointsColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = pointsColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = entry.name.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = formatOperationDate(entry.operationDate),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatOperationDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        
        val now = Calendar.getInstance()
        val opDate = Calendar.getInstance().apply { time = date }
        
        if (now.get(Calendar.YEAR) == opDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == opDate.get(Calendar.DAY_OF_YEAR)) {
            return "Dziś"
        }
        
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (yesterday.get(Calendar.YEAR) == opDate.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == opDate.get(Calendar.DAY_OF_YEAR)) {
            return "Wczoraj"
        }
        
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        dateStr
    }
}
