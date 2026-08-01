package io.github.zorinontop.twojastokrotka

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import io.github.zorinontop.twojastokrotka.network.Offer
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailsScreen(offerId: Int, accessToken: String, initialPoints: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }
    
    var offerData by remember { mutableStateOf<Offer?>(null) }
    var pointsCount by remember { mutableStateOf(initialPoints) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var managedOffer by remember { mutableStateOf<Offer?>(null) }
    var sheetMode by remember { mutableStateOf(RewardSheetMode.ACTIVATE) }

    val loadData = suspend {
        try {
            val bearerToken = "Bearer $accessToken"
            val response = apiService.getOfferDetails(bearerToken, offerId)
            if (response.status == 200 && response.data != null) {
                offerData = response.data
                
                try {
                    val profileResponse = apiService.getPrizes(bearerToken)
                    pointsCount = profileResponse.data?.walletDetails?.balance?.toString() ?: pointsCount
                } catch (e: Exception) {}
            } else {
                error = "Błąd: Brak danych oferty"
            }
        } catch (e: Exception) {
            error = "Błąd połączenia"
        }
    }

    val toggleFavorite = {
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val currentOffer = offerData ?: return@launch
                val response = if (currentOffer.isFavorite) {
                    apiService.deleteFavorite(bearerToken, currentOffer.prize.id)
                } else {
                    apiService.setFavorite(bearerToken, currentOffer.prize.id)
                }
                
                if (response.isSuccessful) {
                    loadData()
                }
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(offerId) {
        isLoading = true
        loadData()
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = StokrotkaGreen)
        }
        return
    }

    val offer = offerData ?: return
    val props = offer.prize.banner.properties

    if (managedOffer != null) {
        ManageRewardSheet(
            offer = managedOffer!!,
            mode = sheetMode,
            accessToken = accessToken,
            apiService = apiService,
            coroutineScope = coroutineScope,
            context = context,
            onDismiss = { managedOffer = null },
            onSuccess = { 
                coroutineScope.launch { loadData() }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = props.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { toggleFavorite() }) {
                        Icon(
                            imageVector = if (offer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (offer.isFavorite) Color.Red else StokrotkaGreen
                        )
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
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = props.imageUrl_L ?: props.imageUrl,
                    contentDescription = props.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (!props.prizeCard.isNullOrEmpty()) {
                    val priceParts = props.prizeCard.split(",")
                    val mainPrice = priceParts.getOrNull(0) ?: "0"
                    val subPrice = priceParts.getOrNull(1) ?: "00"
                    
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        color = Color.White,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = mainPrice, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, lineHeight = 48.sp)
                            Text(text = subPrice, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            Text(
                text = props.additional2 ?: "Limit 1 szt.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(end = 16.dp, top = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
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
                    Text(text = pointsCount, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 28.sp)
                }
            }

            OfferDetailMenuItem(
                label = "Kupon za ${offer.pointCost} Płatków", 
                icon = Icons.Default.ConfirmationNumber, 
                trailingText = "x ${offer.activatedCount}",
                onClick = {
                    if (offer.activatedCount > 0) {
                        sheetMode = RewardSheetMode.DEACTIVATE
                        managedOffer = offer
                    }
                }
            )
            OfferDetailMenuItem("Dodaj do listy zakupowej", Icons.AutoMirrored.Filled.FormatListBulleted)
            OfferDetailMenuItem("Zobacz produkty", Icons.Default.GridView)
            OfferDetailMenuItem("Sklepy objęte promocją", Icons.Default.Store)
            OfferDetailMenuItem(
                label = if (offer.isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych",
                icon = if (offer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                onClick = { toggleFavorite() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (offer.activatedCount > 0) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Button(
                        onClick = { 
                            sheetMode = RewardSheetMode.ACTIVATE
                            managedOffer = offer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AKTYWUJ WIĘCEJ", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { 
                            sheetMode = RewardSheetMode.DEACTIVATE
                            managedOffer = offer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("DEZAKTYWUJ KUPON", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            } else {
                Button(
                    onClick = { 
                        sheetMode = RewardSheetMode.ACTIVATE
                        managedOffer = offer
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AKTYWUJ", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = props.title + " 1 szt.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                AndroidView(
                    factory = { context ->
                        TextView(context).apply {
                            setTextColor(android.graphics.Color.WHITE)
                            textSize = 14f
                        }
                    },
                    update = { view ->
                        val html = props.textHtml ?: ""
                        view.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            @Suppress("DEPRECATION")
                            Html.fromHtml(html)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun OfferDetailMenuItem(label: String, icon: ImageVector, trailingText: String? = null, onClick: () -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(20.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            
            if (trailingText != null) {
                Text(text = trailingText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(end = 12.dp))
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
}
