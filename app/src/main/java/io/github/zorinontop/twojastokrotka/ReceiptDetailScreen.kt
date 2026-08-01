package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.network.TransactionDetailData
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(receiptId: Long, accessToken: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }

    var receiptData by remember { mutableStateOf<TransactionDetailData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(receiptId) {
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val response = apiService.getTransactionDetail(bearerToken, receiptId)
                if (response.status == 200 && response.data != null) {
                    receiptData = response.data
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val topBarDate = remember(receiptData) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(receiptData?.transactionDate ?: "")
            if (date != null) {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
            } else ""
        } catch (e: Exception) { "" }
    }

    Scaffold(
        containerColor = Color(0xFF1C1B1F),
        topBar = {
            TopAppBar(
                title = { Text(topBarDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1B1F)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StokrotkaGreen)
            }
        } else if (receiptData != null) {
            val data = receiptData!!
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF1C1B1F))
            ) {
                Surface(modifier = Modifier.fillMaxWidth().height(60.dp), color = Color(0xFFFFD700)) {
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
                        Text(text = "0,00 zł", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    data.lines?.forEach { line ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = line.productName, color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(text = "%.0f * %,.2f".format(line.quantity, line.originalPrice).replace(".", ","), color = Color.Black, fontSize = 13.sp)
                            }
                            Text(text = "%,.2f".format(line.grossValue).replace(".", ","), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Razem", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
                        Text(text = "%,.2f".format(data.total).replace(".", ","), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(Color.White)
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val width = size.width
                        val height = size.height
                        val segmentWidth = 20.dp.toPx()
                        
                        path.moveTo(0f, 0f)
                        var currentX = 0f
                        while (currentX < width) {
                            path.lineTo(currentX + segmentWidth / 2, height)
                            path.lineTo(currentX + segmentWidth, 0f)
                            currentX += segmentWidth
                        }
                        path.close()
                        drawPath(path, color = Color(0xFF1C1B1F)) // Background color to "cut" the white
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Miejsce zakupu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${data.store?.name}, ${data.store?.address?.street}", color = Color.Gray, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(text = "Numer paragonu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = data.receiptNo, color = Color.Gray)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Data zakupu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = formatTransactionDateFull(data.transactionDate), color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

fun formatTransactionDateFull(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        SimpleDateFormat("yyyy-MM-dd, godz. HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        dateStr
    }
}
