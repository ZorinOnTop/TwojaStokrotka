package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import io.github.zorinontop.twojastokrotka.network.TransactionEntry
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(accessToken: String, onBack: () -> Unit, onReceiptClick: (Long) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }

    var totalSavings by remember { mutableStateOf("0,00") }
    var transactions by remember { mutableStateOf<List<TransactionEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    var isLastPage by remember { mutableStateOf(false) }

    suspend fun loadNextPage() {
        if (isLoading || isLastPage) return
        isLoading = true
        try {
            val bearerToken = "Bearer $accessToken"
            val response = apiService.getTransactionList(bearerToken, currentPage)
            if (response.status == 200 && response.data != null) {
                totalSavings = "%,.2f".format(response.data.savings).replace(".", ",")
                val newList = response.data.transactionList ?: emptyList()
                if (newList.isEmpty()) {
                    isLastPage = true
                } else {
                    transactions = transactions + newList
                    currentPage++
                }
            } else {
                isLastPage = true
            }
        } catch (e: Exception) {
            isLastPage = true
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadNextPage()
    }

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { entry ->
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(entry.transactionDateTime)
                if (date != null) {
                    val out = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    out.format(date).replaceFirstChar { it.uppercase() }
                } else "Nieznany"
            } catch (e: Exception) { "Nieznany" }
        }
    }

    Scaffold(
        containerColor = Color(0xFF1C1B1F),
        topBar = {
            TopAppBar(
                title = { Text("HISTORIA PARAGONÓW", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1B1F),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Surface(modifier = Modifier.fillMaxWidth().height(60.dp), color = Color(0xFFFFD700), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image), contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "OSZCZĘDZIŁEŚ Z NAMI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(text = "$totalSavings zł", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1B1F))) {
                groupedTransactions.forEach { (month, entries) ->
                    item {
                        MonthHeader(month, entries.sumOf { it.savings })
                    }
                    items(entries) { entry ->
                        ReceiptRow(entry) { onReceiptClick(entry.receiptId) }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    }
                }

                if (!isLastPage) {
                    item {
                        LaunchedEffect(Unit) {
                            loadNextPage()
                        }
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = StokrotkaGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(month: String, monthlySavings: Double) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = month, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        Text(text = "Oszczędziłeś %,.2f zł".format(monthlySavings).replace(".", ","), color = Color.Gray, fontSize = 13.sp)
    }
}

@Composable
fun ReceiptRow(entry: TransactionEntry, onClick: () -> Unit) {
    val dateText = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(entry.transactionDateTime)
        if (date != null) {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
        } else entry.transactionDateTime
    } catch (e: Exception) { entry.transactionDateTime }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = dateText, fontSize = 16.sp, color = Color.White)
        Text(text = "%,.2f".format(entry.grossValue).replace(".", ","), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
    }
}
