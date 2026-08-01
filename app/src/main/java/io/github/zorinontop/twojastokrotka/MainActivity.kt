package io.github.zorinontop.twojastokrotka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import io.github.zorinontop.twojastokrotka.models.FilterCriteria
import io.github.zorinontop.twojastokrotka.models.SortOption
import io.github.zorinontop.twojastokrotka.network.Offer
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.ui.theme.TwojaStokrotkaTheme
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class Screen {
    object Loading : Screen()
    object Login : Screen()
    data class Otp(val phoneNumber: String) : Screen()
    data class Dashboard(val accessToken: String) : Screen()
    data class Magazine(val id: String, val page: Int, val accessToken: String) : Screen()
    data class KuponyZaPlatki(
        val accessToken: String,
        val sort: SortOption = SortOption.DOMYSLNE,
        val filter: FilterCriteria = FilterCriteria()
    ) : Screen()
    data class SortFilter(
        val allOffers: List<Offer>,
        val currentSort: SortOption,
        val currentFilter: FilterCriteria,
        val accessToken: String
    ) : Screen()
    data class AboutApp(val accessToken: String) : Screen()
    data class OfferDetails(
        val offerId: Int, 
        val accessToken: String, 
        val pointsCount: String, 
        val previousScreen: Screen
    ) : Screen()
    data class History(val accessToken: String) : Screen()
    data class Rules(val accessToken: String) : Screen()
    data class Transfer(val accessToken: String) : Screen()
    data class TransferHistory(val accessToken: String) : Screen()
    data class Stickers(val accessToken: String) : Screen()
    data class ShoppingLists(val accessToken: String) : Screen()
    data class ShoppingListDetail(val listId: String, val listName: String, val accessToken: String) : Screen()
    data class Receipts(val accessToken: String) : Screen()
    data class ReceiptDetail(val receiptId: Long, val accessToken: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context) }
            val coroutineScope = rememberCoroutineScope()
            
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }
            var showSessionError by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val savedToken = sessionManager.accessToken.first()
                if (savedToken != null) {
                    currentScreen = Screen.Dashboard(savedToken)
                } else {
                    currentScreen = Screen.Login
                }
            }

            TwojaStokrotkaTheme(darkTheme = true, dynamicColor = false) {
                if (showSessionError) {
                    AlertDialog(
                        onDismissRequest = { showSessionError = false },
                        title = { Text("Błąd sesji") },
                        text = { Text("Wystąpił błąd podczas logowania. Zaloguj się ponownie.") },
                        confirmButton = {
                            TextButton(onClick = { 
                                showSessionError = false
                                currentScreen = Screen.Login
                            }) {
                                Text("OK", color = StokrotkaGreen)
                            }
                        }
                    )
                }

                when (val screen = currentScreen) {
                    is Screen.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    }
                    is Screen.Login -> {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                LoginScreen(onLoginSuccess = { phone -> currentScreen = Screen.Otp(phone) })
                            }
                        }
                    }
                    is Screen.Otp -> {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                OtpScreen(
                                    phoneNumber = screen.phoneNumber,
                                    onBack = { currentScreen = Screen.Login },
                                    onVerifySuccess = { token -> currentScreen = Screen.Dashboard(token) }
                                )
                            }
                        }
                    }
                    is Screen.Dashboard -> {
                        MainDashboardScreen(
                            accessToken = screen.accessToken,
                            onLogout = {
                                coroutineScope.launch {
                                    sessionManager.clearSession()
                                    currentScreen = Screen.Login
                                }
                            },
                            onMagazineClick = { id, page ->
                                currentScreen = Screen.Magazine(id, page, screen.accessToken)
                            },
                            onKuponyZaPlatkiClick = {
                                currentScreen = Screen.KuponyZaPlatki(screen.accessToken)
                            },
                            onAuthError = { showSessionError = true },
                            onAboutAppClick = {
                                currentScreen = Screen.AboutApp(screen.accessToken)
                            },
                            onOfferClick = { offerId, points ->
                                currentScreen = Screen.OfferDetails(offerId, screen.accessToken, points, screen)
                            },
                            onHistoryClick = {
                                currentScreen = Screen.History(screen.accessToken)
                            },
                            onRulesClick = {
                                currentScreen = Screen.Rules(screen.accessToken)
                            },
                            onTransferClick = {
                                currentScreen = Screen.Transfer(screen.accessToken)
                            },
                            onStickersClick = {
                                currentScreen = Screen.Stickers(screen.accessToken)
                            },
                            onShoppingListsClick = {
                                currentScreen = Screen.ShoppingLists(screen.accessToken)
                            },
                            onReceiptsClick = {
                                currentScreen = Screen.Receipts(screen.accessToken)
                            }
                        )
                    }
                    is Screen.Magazine -> {
                        MagazineScreen(
                            magazineId = screen.id,
                            initialPage = screen.page,
                            accessToken = screen.accessToken,
                            onBack = {
                                currentScreen = Screen.Dashboard(screen.accessToken)
                            }
                        )
                    }
                    is Screen.KuponyZaPlatki -> {
                        KuponyZaPlatkiScreen(
                            accessToken = screen.accessToken,
                            initialSort = screen.sort,
                            initialFilter = screen.filter,
                            onBack = {
                                currentScreen = Screen.Dashboard(screen.accessToken)
                            },
                            onNavigateToFilter = { allOffers, sort, filter ->
                                currentScreen = Screen.SortFilter(allOffers, sort, filter, screen.accessToken)
                            },
                            onOfferClick = { offerId, points ->
                                currentScreen = Screen.OfferDetails(offerId, screen.accessToken, points, screen)
                            }
                        )
                    }
                    is Screen.SortFilter -> {
                        SortFilterScreen(
                            initialSort = screen.currentSort,
                            initialFilter = screen.currentFilter,
                            allOffers = screen.allOffers,
                            onApply = { sort, filter ->
                                currentScreen = Screen.KuponyZaPlatki(screen.accessToken, sort, filter)
                            },
                            onBack = {
                                currentScreen = Screen.KuponyZaPlatki(screen.accessToken, screen.currentSort, screen.currentFilter)
                            }
                        )
                    }
                    is Screen.AboutApp -> {
                        AboutAppScreen(
                            onBack = {
                                currentScreen = Screen.Dashboard(screen.accessToken)
                            }
                        )
                    }
                    is Screen.OfferDetails -> {
                        OfferDetailsScreen(
                            offerId = screen.offerId,
                            accessToken = screen.accessToken,
                            initialPoints = screen.pointsCount,
                            onBack = {
                                currentScreen = screen.previousScreen
                            }
                        )
                    }
                    is Screen.History -> {
                        HistoryScreen(
                            accessToken = screen.accessToken,
                            onBack = {
                                currentScreen = Screen.Dashboard(screen.accessToken)
                            }
                        )
                    }
                    is Screen.Rules -> {
                        RulesScreen(
                            onBack = {
                                currentScreen = Screen.Dashboard(screen.accessToken)
                            }
                        )
                    }
                    is Screen.Transfer -> {
                        TransferScreen(
                            accessToken = screen.accessToken,
                            onBack = { currentScreen = Screen.Dashboard(screen.accessToken) },
                            onNavigateToHistory = { currentScreen = Screen.TransferHistory(screen.accessToken) }
                        )
                    }
                    is Screen.TransferHistory -> {
                        TransferHistoryScreen(
                            accessToken = screen.accessToken,
                            onBack = { currentScreen = Screen.Dashboard(screen.accessToken) },
                            onNavigateToTransfer = { currentScreen = Screen.Transfer(screen.accessToken) }
                        )
                    }
                    is Screen.Stickers -> {
                        StickersScreen(
                            onBack = {
                                currentScreen = Screen.Dashboard(screen.accessToken)
                            }
                        )
                    }
                    is Screen.ShoppingLists -> {
                        ShoppingListsScreen(
                            accessToken = screen.accessToken,
                            onBack = { currentScreen = Screen.Dashboard(screen.accessToken) },
                            onListClick = { id, name ->
                                currentScreen = Screen.ShoppingListDetail(id, name, screen.accessToken)
                            }
                        )
                    }
                    is Screen.ShoppingListDetail -> {
                        ShoppingListDetailScreen(
                            listId = screen.listId,
                            listName = screen.listName,
                            accessToken = screen.accessToken,
                            onBack = { currentScreen = Screen.ShoppingLists(screen.accessToken) }
                        )
                    }
                    is Screen.Receipts -> {
                        ReceiptsScreen(
                            accessToken = screen.accessToken,
                            onBack = { currentScreen = Screen.Dashboard(screen.accessToken) },
                            onReceiptClick = { id -> currentScreen = Screen.ReceiptDetail(id, screen.accessToken) }
                        )
                    }
                    is Screen.ReceiptDetail -> {
                        ReceiptDetailScreen(
                            receiptId = screen.receiptId,
                            accessToken = screen.accessToken,
                            onBack = { currentScreen = Screen.Receipts(screen.accessToken) }
                        )
                    }
                }
            }
        }
    }
}
