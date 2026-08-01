package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.zorinontop.twojastokrotka.models.FilterCriteria
import io.github.zorinontop.twojastokrotka.models.SortOption
import io.github.zorinontop.twojastokrotka.network.Offer
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuponyZaPlatkiScreen(
    accessToken: String,
    initialSort: SortOption,
    initialFilter: FilterCriteria,
    onNavigateToFilter: (List<Offer>, SortOption, FilterCriteria) -> Unit,
    onOfferClick: (Int, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }
    
    var rewards by remember { mutableStateOf<List<Offer>>(emptyList()) }
    var pointsCount by remember { mutableStateOf("0") }
    var isLoading by remember { mutableStateOf(true) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var managedOffer by remember { mutableStateOf<Offer?>(null) }
    var sheetMode by remember { mutableStateOf(RewardSheetMode.ACTIVATE) }

    val refreshData = {
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val response = apiService.getPrizes(bearerToken)
                rewards = response.data?.offers ?: emptyList()
                pointsCount = response.data?.walletDetails?.balance?.toString() ?: pointsCount
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(accessToken) {
        coroutineScope.launch {
            try {
                val bearerToken = "Bearer $accessToken"
                val response = apiService.getPrizes(bearerToken)
                rewards = response.data?.offers ?: emptyList()
                pointsCount = response.data?.walletDetails?.balance?.toString() ?: pointsCount
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val processedRewards = remember(rewards, initialSort, initialFilter, showOnlyFavorites, searchQuery) {
        var list = rewards
        
        if (searchQuery.isNotEmpty()) {
            list = list.filter { it.prize.banner.properties.title.contains(searchQuery, ignoreCase = true) }
        }
        
        if (showOnlyFavorites) {
            list = list.filter { it.isFavorite }
        }
        if (initialFilter.selectedPetals.isNotEmpty()) {
            list = list.filter { initialFilter.selectedPetals.contains(it.pointCost) }
        }
        if (initialFilter.selectedCategories.isNotEmpty()) {
            list = list.filter { initialFilter.selectedCategories.contains(it.category) }
        }

        when (initialSort) {
            SortOption.PO_KATEGORII -> list = list.sortedBy { it.category }
            SortOption.OD_NAJNIZSZEJ_CENY -> list = list.sortedBy { it.prize.banner.properties.prizeCard?.replace(",", ".")?.toDoubleOrNull() ?: 0.0 }
            SortOption.OD_NAJWYZSZEJ_CENY -> list = list.sortedByDescending { it.prize.banner.properties.prizeCard?.replace(",", ".")?.toDoubleOrNull() ?: 0.0 }
            SortOption.OD_NAJNIZSZEJ_ILOSCI_PLATKOW -> list = list.sortedBy { it.pointCost }
            SortOption.OD_NAJWYZSZEJ_ILOSCI_PLATKOW -> list = list.sortedByDescending { it.pointCost }
            else -> {
                list = list.sortedByDescending { it.activatedCount }
            }
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchMode) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Szukaj nagrody...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { 
                                    if (searchQuery.isNotEmpty()) searchQuery = "" else isSearchMode = false 
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                                }
                            }
                        )
                    } else {
                        Text(
                            text = "KUPONY ZA PŁATKI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (isSearchMode) isSearchMode = false else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isSearchMode) {
                        IconButton(onClick = { isSearchMode = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { 
                            onNavigateToFilter(rewards, initialSort, initialFilter)
                        }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
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

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                color = Color(0xFFFFD700)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Twoje PŁATKI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(text = pointsCount, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Pokaż tylko ulubione kupony", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = showOnlyFavorites,
                    onCheckedChange = { showOnlyFavorites = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = StokrotkaGreen)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StokrotkaGreen)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(processedRewards) { offer ->
                        RewardGridCard(
                            offer = offer,
                            onClick = { id -> onOfferClick(id, pointsCount) },
                            onRedeemClick = { off, mode ->
                                managedOffer = off
                                sheetMode = mode
                            },
                            onToggleFavorite = { off ->
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
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardGridCard(offer: Offer, onClick: (Int) -> Unit, onRedeemClick: (Offer, RewardSheetMode) -> Unit, onToggleFavorite: (Offer) -> Unit) {
    val props = offer.prize.banner.properties
    
    Card(
        modifier = Modifier.fillMaxWidth().height(280.dp).clickable { onClick(offer.offerId) },
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
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))

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
                    contentPadding = PaddingValues(0.0.dp)
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
                    Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${offer.pointCost} PŁATKÓW", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
