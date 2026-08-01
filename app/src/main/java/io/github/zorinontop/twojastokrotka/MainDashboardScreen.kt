package io.github.zorinontop.twojastokrotka

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import io.github.zorinontop.twojastokrotka.network.*
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class DashboardTab {
    Dom, Oferty, Platki, Gazetki, Wiecej
}


fun formatDateRange(from: String, to: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("dd.MM", Locale.getDefault())
        val outputWithYear = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        
        val dateFrom = input.parse(from)
        val dateTo = input.parse(to)
        
        if (dateFrom != null && dateTo != null) {
            "${output.format(dateFrom)} - ${outputWithYear.format(dateTo)}"
        } else ""
    } catch (e: Exception) {
        ""
    }
}

fun calculateDaysLeft(activeTo: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val expiryDate = sdf.parse(activeTo)
        if (expiryDate != null) {
            val diff = expiryDate.time - System.currentTimeMillis()
            (diff / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        } else 0
    } catch (e: Exception) {
        0
    }
}


@Composable
fun SectionHeader(title: String, onMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMoreClick() }
        ) {
            Text(text = "Więcej", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StoreInfoCard(storeInfo: StoreData?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(StokrotkaGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = StokrotkaGreen)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Twój sklep", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (storeInfo != null) "${storeInfo.address.street}, ${storeInfo.address.city}" else "Wyszukiwanie sklepu...",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = StokrotkaGreen)
        }
    }
}

@Composable
fun MagazineThumbnailCard(magazine: MagazineData, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick(magazine.id.toString()) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = magazine.banner?.properties?.imageUrl ?: magazine.pages?.firstOrNull()?.imageUrl,
                contentDescription = magazine.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = formatDateRange(magazine.dateFrom ?: "", magazine.dateTo ?: ""),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RewardCard(offer: Offer, onClick: (Int) -> Unit, onRedeemClick: (Offer, RewardSheetMode) -> Unit, onToggleFavorite: (Offer) -> Unit) {
    val props = offer.prize.banner.properties
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .clickable { onClick(offer.offerId) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Icon(
                    imageVector = if (offer.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clickable { onToggleFavorite(offer) },
                    tint = if (offer.isFavorite) Color.Red else StokrotkaGreen
                )
                
                AsyncImage(
                    model = props.imageUrl_M ?: props.imageUrl ?: props.imageUrl_L,
                    contentDescription = props.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )

                if (!props.prizeCard.isNullOrEmpty()) {
                    val priceParts = props.prizeCard.split(",")
                    val mainPrice = priceParts.getOrNull(0) ?: "0"
                    val subPrice = priceParts.getOrNull(1) ?: "00"
                    
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = mainPrice, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 24.sp)
                            Text(text = subPrice, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
            
            Text(
                text = props.additional2 ?: "Limit 1 szt.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
            )
            
            Text(
                text = props.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.height(40.dp).padding(top = 4.dp),
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            if (offer.activatedCount > 0) {
                OutlinedButton(
                    onClick = { onRedeemClick(offer, RewardSheetMode.DEACTIVATE) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AKTYWNY x ${offer.activatedCount}", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Button(
                    onClick = { onRedeemClick(offer, RewardSheetMode.ACTIVATE) },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(painterResource(id = android.R.drawable.ic_menu_info_details), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${offer.pointCost} PŁATKÓW", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun CouponCard(prize: UserPrize) {
    val props = prize.banner.properties
    val daysLeft = remember(prize.activeTo) { calculateDaysLeft(prize.activeTo) }

    Card(
        modifier = Modifier.width(300.dp).fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                AsyncImage(
                    model = props.imageUrl,
                    contentDescription = props.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                if (!props.additional1.isNullOrEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomEnd = 4.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = props.additional1!!,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = props.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = "$daysLeft dni do końca", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { /* Activate */ },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AKTYWUJ", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionCard(prize: UserPrize) {
    val props = prize.banner.properties
    
    Card(
        modifier = Modifier.width(160.dp).fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                Icon(
                    imageVector = if (prize.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp),
                    tint = if (prize.isFavorite == true) Color.Red else StokrotkaGreen
                )
                
                AsyncImage(
                    model = props.imageUrl_M ?: props.imageUrl,
                    contentDescription = props.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Fit
                )

                if (!props.prizeCard.isNullOrEmpty()) {
                    val priceParts = props.prizeCard.split(",")
                    val mainPrice = priceParts.getOrNull(0) ?: "0"
                    val subPrice = priceParts.getOrNull(1) ?: "00"
                    
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = mainPrice, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 24.sp)
                            Text(text = subPrice, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
            
            Text(
                text = props.additional2 ?: "",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                maxLines = 1
            )
            
            Text(
                text = props.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.height(40.dp).padding(top = 4.dp),
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = formatDateRange(prize.activeFrom, prize.activeTo),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FullWidthCouponCard(prize: UserPrize) {
    val props = prize.banner.properties
    val daysLeft = remember(prize.activeTo) { calculateDaysLeft(prize.activeTo) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = props.imageUrl,
                    contentDescription = props.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                if (!props.additional1.isNullOrEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomEnd = 4.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = props.additional1!!,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = props.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(text = "$daysLeft dni do końca", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = { /* Activate */ },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AKTYWUJ", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun FullWidthPromotionCard(prize: UserPrize) {
    val props = prize.banner.properties
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Icon(
                    imageVector = if (prize.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp),
                    tint = if (prize.isFavorite == true) Color.Red else StokrotkaGreen
                )
                
                AsyncImage(
                    model = props.imageUrl_M ?: props.imageUrl,
                    contentDescription = props.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (!props.prizeCard.isNullOrEmpty()) {
                    val priceParts = props.prizeCard.split(",")
                    val mainPrice = priceParts.getOrNull(0) ?: "0"
                    val subPrice = priceParts.getOrNull(1) ?: "00"
                    
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = mainPrice, color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 28.sp)
                            Text(text = subPrice, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = props.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = formatDateRange(prize.activeFrom, prize.activeTo), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!props.additional2.isNullOrEmpty()) {
                    Text(text = props.additional2!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SeeMoreCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "Zobacz więcej",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun MainDashboardScreen(
    accessToken: String, 
    onLogout: () -> Unit,
    onMagazineClick: (String, Int) -> Unit,
    onKuponyZaPlatkiClick: () -> Unit,
    onAuthError: () -> Unit,
    onAboutAppClick: () -> Unit,
    onOfferClick: (Int, String) -> Unit,
    onHistoryClick: () -> Unit,
    onRulesClick: () -> Unit,
    onTransferClick: () -> Unit,
    onStickersClick: () -> Unit,
    onShoppingListsClick: () -> Unit,
    onReceiptsClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context), onAuthError) }
    
    var selectedTab by remember { mutableStateOf(DashboardTab.Dom) }
    var offersSubTab by remember { mutableStateOf(0) }
    var magazinesSubTab by remember { mutableStateOf(0) }
    
    var userName by remember { mutableStateOf("...") }
    var totalSavings by remember { mutableStateOf("0") }
    var pointsCount by remember { mutableStateOf("0") }
    var unreadMessagesCount by remember { mutableStateOf(0) }
    var storeInfo by remember { mutableStateOf<StoreData?>(null) }
    var recommendedOffers by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var banners by remember { mutableStateOf<List<StokrotkaBanner>>(emptyList()) }
    var homeCoupons by remember { mutableStateOf<List<UserPrizeData>>(emptyList()) }
    var homeAppOffers by remember { mutableStateOf<List<UserPrize>>(emptyList()) }
    var magazineOffers by remember { mutableStateOf<List<UserPrize>>(emptyList()) }
    var allMagazines by remember { mutableStateOf<List<MagazineData>>(emptyList()) }
    
    var fullCoupons by remember { mutableStateOf<List<UserPrizeData>>(emptyList()) }
    var fullAppOffers by remember { mutableStateOf<List<UserPrize>>(emptyList()) }
    
    var managedOffer by remember { mutableStateOf<Offer?>(null) }
    var sheetMode by remember { mutableStateOf(RewardSheetMode.ACTIVATE) }
    
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var cardNumber by remember { mutableStateOf("") }
    var showKartaSheet by remember { mutableStateOf(false) }

    val refreshData = {
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val prizesResponse = apiService.getPrizes(bearerToken)
                pointsCount = prizesResponse.data?.walletDetails?.balance?.toString() ?: pointsCount
                val allOffers = prizesResponse.data?.offers ?: emptyList()
                recommendedOffers = allOffers.sortedByDescending { it.activatedCount }.take(20)
            } catch (e: Exception) {}
        }
    }

    val onToggleFavorite: (Offer) -> Unit = { off ->
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val response = if (off.isFavorite) {
                    apiService.deleteFavorite(bearerToken, off.prize.id)
                } else {
                    apiService.setFavorite(bearerToken, off.prize.id)
                }
                if (response.isSuccessful) refreshData()
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(accessToken) {
        coroutineScope.launch {
            try {
                isLoading = true
                val bearerToken = "Bearer $accessToken"
                val user = apiService.getUserProfile(bearerToken)
                userName = user.data?.firstName ?: "..."
                totalSavings = "%,.2f".format(user.data?.totalSavings ?: 0.0).replace(",", " ").replace(".", ",")
                cardNumber = user.data?.loyaltyCardNumber ?: ""

                val jobs = listOf(
                    launch { 
                        try {
                            val prizesResponse = apiService.getPrizes(bearerToken)
                            pointsCount = prizesResponse.data?.walletDetails?.balance?.toString() ?: pointsCount
                            val allOffers = prizesResponse.data?.offers ?: emptyList()
                            recommendedOffers = allOffers.sortedByDescending { it.activatedCount }.take(20)
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val messages = apiService.getMessages(bearerToken)
                            unreadMessagesCount = messages.data?.count { !it.isOpen } ?: 0
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val bannerResponse = apiService.getBanners(bearerToken)
                            banners = bannerResponse.data ?: emptyList()
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val couponsResponse = apiService.getUserPrizes(bearerToken, pageSize = 50)
                            fullCoupons = couponsResponse.data ?: emptyList()
                            homeCoupons = couponsResponse.data?.take(5) ?: emptyList()
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val response = apiService.getPrizesByPath(bearerToken, "/application_screens/home/oferty", pageSize = 50)
                            fullAppOffers = response.data ?: emptyList()
                            homeAppOffers = response.data?.take(5) ?: emptyList()
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val response = apiService.getPrizesByPath(bearerToken, "/application_screens/home/oferty_gazetka")
                            magazineOffers = response.data ?: emptyList()
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val response = apiService.getAllMagazines(bearerToken)
                            allMagazines = response.data ?: emptyList()
                        } catch (e: Exception) {}
                    },
                    launch {
                        try {
                            val shopId = user.data?.properties?.get("my_shop")
                            if (shopId != null) {
                                val storeResponse = apiService.getStore(bearerToken, shopId)
                                storeInfo = storeResponse.data
                            }
                        } catch (e: Exception) {}
                    }
                )
                
                jobs.joinAll()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Wyloguj") },
            text = { Text("Czy na pewno chcesz się wylogować?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Tak", color = StokrotkaGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Anuluj", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { StokrotkaBottomNavigation(selectedTab, onTabSelected = { selectedTab = it }) },
        floatingActionButton = { 
            if (selectedTab == DashboardTab.Dom || selectedTab == DashboardTab.Oferty) {
                StokrotkaKartaButton(onClick = { showKartaSheet = true })
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        if (managedOffer != null) {
            ManageRewardSheet(
                offer = managedOffer!!,
                mode = sheetMode,
                accessToken = accessToken,
                apiService = apiService,
                coroutineScope = coroutineScope,
                context = context,
                onDismiss = { managedOffer = null },
                onSuccess = { refreshData() }
            )
        }

        if (showKartaSheet) {
            LoyaltyCardSheet(
                cardNumber = cardNumber,
                onDismiss = { showKartaSheet = false }
            )
        }

        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                DashboardTab.Dom -> HomeScreen(
                    userName, pointsCount, totalSavings, recommendedOffers, banners, homeCoupons, homeAppOffers, magazineOffers, allMagazines, storeInfo, isLoading,
                    onBannerClick = { link ->
                        try {
                            val uri = link.toUri()
                            if (uri.scheme == "stokrotka" && uri.host == "newspaper_details") {
                                onMagazineClick(uri.pathSegments.firstOrNull() ?: "", uri.getQueryParameter("page")?.toIntOrNull() ?: 1)
                            }
                        } catch (e: Exception) {}
                    },
                    onMagazineClick = { id -> onMagazineClick(id, 1) },
                    onNavigateToRewards = onKuponyZaPlatkiClick,
                    onNavigateToCoupons = { offersSubTab = 0; selectedTab = DashboardTab.Oferty },
                    onNavigateToOffers = { offersSubTab = 1; selectedTab = DashboardTab.Oferty },
                    onOfferClick = onOfferClick,
                    onRedeemClick = { offer, mode ->
                        managedOffer = offer
                        sheetMode = mode
                    },
                    onToggleFavorite = onToggleFavorite
                )
                DashboardTab.Oferty -> OffersTabScreen(offersSubTab, onSubTabSelected = { offersSubTab = it }, fullCoupons, fullAppOffers, isLoading)
                DashboardTab.Platki -> PlatkiTabScreen(pointsCount, onKuponyZaPlatkiClick, onHistoryClick, onRulesClick, onTransferClick)
                DashboardTab.Gazetki -> GazetkiTabScreen(magazinesSubTab, onSubTabSelected = { magazinesSubTab = it }, allMagazines, isLoading, onMagazineClick = { id -> onMagazineClick(id, 1) })
                DashboardTab.Wiecej -> MoreScreen(userName, unreadMessagesCount, storeInfo, onLogoutClick = { showLogoutDialog = true }, onAboutAppClick = onAboutAppClick, onStickersClick = onStickersClick, onShoppingListsClick = onShoppingListsClick, onReceiptsClick = onReceiptsClick, onTransferClick = onTransferClick)
                else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Ekran ${selectedTab.name} w budowie") }
            }
        }
    }
}

@Composable
fun HomeScreen(
    userName: String, pointsCount: String, totalSavings: String, offers: List<Offer>, banners: List<StokrotkaBanner>,
    coupons: List<UserPrizeData>, appOffers: List<UserPrize>, magazineOffers: List<UserPrize>, allMagazines: List<MagazineData>,
    storeInfo: StoreData?, isLoading: Boolean, onBannerClick: (String) -> Unit, onMagazineClick: (String) -> Unit,
    onNavigateToRewards: () -> Unit, onNavigateToCoupons: () -> Unit, onNavigateToOffers: () -> Unit,
    onOfferClick: (Int, String) -> Unit,
    onRedeemClick: (Offer, RewardSheetMode) -> Unit,
    onToggleFavorite: (Offer) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Cześć, $userName", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row { Text(text = "Masz "); Text(text = "$pointsCount Płatków", color = StokrotkaGreen, fontWeight = FontWeight.Bold) }
                    Row { Text(text = "Oszczędziłeś z nami ", fontSize = 14.sp); Text(text = "$totalSavings zł", color = StokrotkaGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                }
                OutlinedButton(onClick = { onNavigateToRewards() }, shape = RoundedCornerShape(8.dp)) {
                    Text("Więcej kuponów", fontSize = 12.sp)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = StokrotkaGreen) }
        } else if (offers.isNotEmpty()) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(280.dp)) {
                items(offers) { offer -> 
                    RewardCard(
                        offer = offer,
                        onClick = { id -> onOfferClick(id, pointsCount) },
                        onRedeemClick = onRedeemClick,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                item { SeeMoreCard(onClick = onNavigateToRewards) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (banners.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { banners.size })
            Column {
                HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 16.dp), pageSpacing = 12.dp, modifier = Modifier.fillMaxWidth().height(200.dp)) { pageIndex ->
                    AsyncImage(model = banners[pageIndex].properties.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).clickable { banners[pageIndex].properties.linkUrl?.let { onBannerClick(it) } }, contentScale = ContentScale.Crop)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                    repeat(banners.size) { iteration ->
                        Box(modifier = Modifier.padding(horizontal = 2.dp).clip(CircleShape).background(if (pagerState.currentPage == iteration) StokrotkaGreen else Color.Gray.copy(alpha = 0.3f)).weight(1f).fillMaxHeight())
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        if (coupons.isNotEmpty()) {
            SectionHeader("Kupony jednorazowe", onMoreClick = onNavigateToCoupons)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(260.dp)) {
                items(coupons) { couponData -> CouponCard(couponData.prize) }
            }
        }

        if (appOffers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader("Oferty z aplikacją", onMoreClick = onNavigateToOffers)
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(280.dp)) {
                items(appOffers) { prize -> PromotionCard(prize) }
            }
        }

        if (magazineOffers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader("Wybrane dla Ciebie z gazetki Supermarket", onMoreClick = {})
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(280.dp)) {
                items(magazineOffers) { prize -> PromotionCard(prize) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        StoreInfoCard(storeInfo)

        Spacer(modifier = Modifier.height(32.dp))
        if (allMagazines.isNotEmpty()) {
            SectionHeader("Gazetki", onMoreClick = {})
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(320.dp)) {
                items(allMagazines) { magazine -> MagazineThumbnailCard(magazine, onMagazineClick) }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun OffersTabScreen(subTab: Int, onSubTabSelected: (Int) -> Unit, coupons: List<UserPrizeData>, appOffers: List<UserPrize>, isLoading: Boolean) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
            Text(text = "KUPONY", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        TabRow(
            selectedTabIndex = subTab, containerColor = MaterialTheme.colorScheme.background, contentColor = StokrotkaGreen,
            indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]), color = StokrotkaGreen) },
            divider = {}
        ) {
            Tab(selected = subTab == 0, onClick = { onSubTabSelected(0) }, text = { Text("Kupony", fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal) })
            Tab(selected = subTab == 1, onClick = { onSubTabSelected(1) }, text = { Text("Oferty", fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal) })
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = StokrotkaGreen) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (subTab == 0) items(coupons) { couponData -> FullWidthCouponCard(couponData.prize) }
                else items(appOffers) { prize -> FullWidthPromotionCard(prize) }
            }
        }
    }
}

@Composable
fun MoreScreen(userName: String, unreadCount: Int, storeInfo: StoreData?, onLogoutClick: () -> Unit, onAboutAppClick: () -> Unit, onStickersClick: () -> Unit, onShoppingListsClick: () -> Unit, onReceiptsClick: () -> Unit, onTransferClick: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(text = "Cześć, $userName", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            BadgedBox(badge = { if (unreadCount > 0) Badge(containerColor = Color.Red, contentColor = Color.White) { Text(unreadCount.toString()) } }) {
                Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = "Notifications", modifier = Modifier.size(32.dp))
            }
        }
        StoreInfoCard(storeInfo)
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        val menuItems = listOf(
            Triple("Akcje znaczkowe", Icons.Default.PieChart, onStickersClick), 
            Triple("Listy zakupowe", Icons.AutoMirrored.Filled.Assignment, onShoppingListsClick),
            Triple("Historia paragonów", Icons.Default.MenuBook, onReceiptsClick), 
            Triple("Mapa sklepów", Icons.Default.Map, {
                android.widget.Toast.makeText(context, "Mapa sklepów już niedługo!", android.widget.Toast.LENGTH_SHORT).show()
            }), 
            Triple("Przekaż Płatki lub Znaczki", Icons.Default.SwapHoriz, onTransferClick),
            Triple("Informacja o aplikacji", Icons.Default.Info, onAboutAppClick),
            Triple("Wyloguj", Icons.AutoMirrored.Filled.Logout, onLogoutClick)
        )
        menuItems.forEach { (label, icon, onClick) ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(20.dp))
                Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StokrotkaBottomNavigation(selectedTab: DashboardTab, onTabSelected: (DashboardTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
        val items = listOf(Triple(DashboardTab.Dom, Icons.Default.Home, "Dom"), Triple(DashboardTab.Oferty, Icons.Default.LocalOffer, "Oferty"), Triple(DashboardTab.Platki, Icons.Default.AccountBalanceWallet, "Płatki"), Triple(DashboardTab.Gazetki, Icons.Default.MenuBook, "Gazetki"), Triple(DashboardTab.Wiecej, Icons.Default.MoreHoriz, "Więcej"))
        items.forEach { (tab, icon, label) ->
            NavigationBarItem(
                selected = selectedTab == tab, onClick = { onTabSelected(tab) }, icon = { Icon(icon, contentDescription = label) }, label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = StokrotkaGreen, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, selectedTextColor = StokrotkaGreen, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, indicatorColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun StokrotkaKartaButton(onClick: () -> Unit) {
    Surface(onClick = onClick, color = StokrotkaGreen, shape = RoundedCornerShape(12.dp), shadowElevation = 6.dp, modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("KARTA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun PlatkiTabScreen(pointsCount: String, onKuponyZaPlatkiClick: () -> Unit, onHistoryClick: () -> Unit, onRulesClick: () -> Unit, onTransferClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PROGRAM PŁATKI",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = StokrotkaGreen.copy(alpha = 0.2f)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(60.dp),
            color = Color(0xFFFFD700),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Twoje PŁATKI",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = pointsCount,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                )
            }
        }

        val menuItems = listOf(
            Triple("Kupony za Płatki", Icons.Default.ConfirmationNumber, onKuponyZaPlatkiClick),
            Triple("Historia Płatków", Icons.Default.Eco, onHistoryClick),
            Triple("Zasady programu", Icons.Default.Description, onRulesClick),
            Triple("Przekaż Płatki", Icons.Default.SwapHoriz, onTransferClick)
        )

        menuItems.forEach { (label, icon, onClick) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rób zakupy i zbieraj Płatki!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Za każde* wydane 10 zł otrzymasz 1 Płatek.\nZbieraj i wymieniaj na wybrane kupony rabatowe, które możesz zdobyć już nawet za 5 Płatków!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wybieraj z puli ponad 100 SuperKuponów!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Produkty i usługi za które nie są przyznawane Płatki:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val exclusions = listOf(
                    "* napoje alkoholowe,",
                    "* wyroby tytoniowe,",
                    "* lekarstwa,",
                    "* preparaty do początkowego żywienia niemowląt,",
                    "* doładowania do telefonów,",
                    "* kaucja za butelki."
                )
                exclusions.forEach { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Dzienny limit Płatków, które może otrzymać użytkownik: 50",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Szczegóły w regulaminie.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun GazetkiTabScreen(
    subTab: Int,
    onSubTabSelected: (Int) -> Unit,
    allMagazines: List<MagazineData>,
    isLoading: Boolean,
    onMagazineClick: (String) -> Unit
) {
    val filteredMagazines = remember(subTab, allMagazines) {
        if (subTab == 0) allMagazines.filter { it.isUserStoreMagazine }
        else allMagazines
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
            Text(text = "GAZETKI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = StokrotkaGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[subTab]),
                    color = StokrotkaGreen
                )
            },
            divider = {}
        ) {
            Tab(selected = subTab == 0, onClick = { onSubTabSelected(0) }, text = { Text("W Twoim sklepie", fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (subTab == 0) Color.White else Color.Gray) })
            Tab(selected = subTab == 1, onClick = { onSubTabSelected(1) }, text = { Text("Wszystkie", fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (subTab == 1) Color.White else Color.Gray) })
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StokrotkaGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allMagazines.filter { if (subTab == 0) it.isUserStoreMagazine else true }) { magazine ->
                    MagazineListItem(magazine, onMagazineClick)
                }
            }
        }
    }
}

@Composable
fun MagazineListItem(magazine: MagazineData, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(magazine.id.toString()) },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = magazine.banner?.properties?.imageUrl ?: magazine.pages?.firstOrNull()?.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = magazine.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDateRangeSimple(magazine.dateFrom ?: "", magazine.dateTo ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}

fun formatDateRangeSimple(from: String, to: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFull = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        
        val dateFrom = input.parse(from)
        val dateTo = input.parse(to)
        
        if (dateFrom != null && dateTo != null) {
            "${SimpleDateFormat("dd.MM", Locale.getDefault()).format(dateFrom)} - ${outputFull.format(dateTo)}"
        } else ""
    } catch (e: Exception) { "" }
}
