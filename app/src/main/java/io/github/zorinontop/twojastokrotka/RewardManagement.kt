package io.github.zorinontop.twojastokrotka

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.github.zorinontop.twojastokrotka.network.Offer
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class RewardSheetMode {
    ACTIVATE, DEACTIVATE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRewardSheet(
    offer: Offer,
    mode: RewardSheetMode,
    accessToken: String,
    apiService: StokrotkaApiService,
    coroutineScope: CoroutineScope,
    context: Context,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedCount by remember { mutableIntStateOf(1) }
    var isProcessing by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black, // Full black for consistency
        contentColor = Color.White,
        dragHandle = null
    ) {
        QuantitySelectorContent(
            title = if (mode == RewardSheetMode.ACTIVATE) "Aktywuj za ${offer.pointCost * selectedCount} Płatków" else "Dezaktywuj",
            count = selectedCount,
            maxCount = if (mode == RewardSheetMode.ACTIVATE) 99 else offer.activatedCount,
            onCountChange = { selectedCount = it },
            isProcessing = isProcessing,
            onSave = {
                coroutineScope.launch {
                    isProcessing = true
                    try {
                        val bearerToken = "Bearer $accessToken"
                        val response = if (mode == RewardSheetMode.ACTIVATE) {
                            apiService.activateOffer(bearerToken, offer.offerId, selectedCount)
                        } else {
                            apiService.deactivateOffer(bearerToken, offer.offerId, selectedCount)
                        }

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Zapisano pomyślnie", Toast.LENGTH_SHORT).show()
                            onSuccess()
                            onDismiss()
                        } else {
                            val errorBody = try { response.errorBody()?.string() ?: "" } catch (e: Exception) { "" }
                            if (errorBody.contains("User lack funds", ignoreCase = true)) {
                                Toast.makeText(context, "Niewystarczająco płatków", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Błąd: ${response.code()}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Błąd połączenia", Toast.LENGTH_LONG).show()
                    } finally {
                        isProcessing = false
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoyaltyCardSheet(cardNumber: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val qrBitmap = remember(cardNumber) { generateQRCode(cardNumber, 600) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black, // Full black background
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Zeskanuj swój kod",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.Black, // Dark text on white card
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("Za każde wydane ")
                            withStyle(style = SpanStyle(color = StokrotkaGreen, fontWeight = FontWeight.Black)) {
                                append("10 zł")
                            }
                            append("\notrzymasz ")
                            withStyle(style = SpanStyle(color = StokrotkaGreen, fontWeight = FontWeight.Black)) {
                                append("1 Płatek")
                            }
                            append(".")
                        },
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    
                    Text(
                        text = "Pamiętaj o aktywowaniu Kuponów!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = StokrotkaGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = cardNumber,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

@Composable
fun QuantitySelectorContent(
    title: String,
    count: Int,
    maxCount: Int,
    onCountChange: (Int) -> Unit,
    onSave: () -> Unit,
    isProcessing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Kupon", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (count > 1) onCountChange(count - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus", tint = Color.White)
                }
                
                Text(
                    text = count.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                IconButton(onClick = { if (count < maxCount) onCountChange(count + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("ZAPISZ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
