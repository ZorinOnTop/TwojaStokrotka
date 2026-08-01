package io.github.zorinontop.twojastokrotka

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.zorinontop.twojastokrotka.network.MagazineData
import io.github.zorinontop.twojastokrotka.network.StokrotkaApiService
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen
import io.github.zorinontop.twojastokrotka.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineScreen(magazineId: String, initialPage: Int, accessToken: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { StokrotkaApiService.create(SessionManager(context)) }
    var magazineData by remember { mutableStateOf<MagazineData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isGridView by remember { mutableStateOf(false) }

    fun sharePage(url: String, isPdf: Boolean = false) {
        val message = if (isPdf) "Zobacz całą gazetkę Stokrotki w PDF: $url" else "Sprawdź tę stronę z gazetki Stokrotki: $url"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    fun downloadPage(url: String, title: String, isPdf: Boolean = false) {
        try {
            val extension = if (isPdf) "pdf" else "jpg"
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription(if (isPdf) "Pobieranie gazetki PDF..." else "Pobieranie strony gazetki...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$title.$extension")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            Toast.makeText(context, "Pobieranie rozpoczęte", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Błąd pobierania: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(magazineId) {
        coroutineScope.launch {
            try {
                val response = apiService.getMagazine("Bearer $accessToken", magazineId)
                magazineData = response.data
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = StokrotkaGreen)
        }
        return
    }

    val data = magazineData ?: return
    val pages = data.pages ?: emptyList()
    
    val pagerState = rememberPagerState(initialPage = (initialPage - 1).coerceIn(0, pages.size.coerceAtLeast(1) - 1)) {
        pages.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = data.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (isGridView) {
                            data.linkUrl?.let { sharePage(it, isPdf = true) }
                        } else {
                            val currentPageUrl = pages.getOrNull(pagerState.currentPage)?.imageUrl
                            currentPageUrl?.let { sharePage(it) }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.Close else Icons.Default.GridView, 
                            contentDescription = "Toggle Grid",
                            tint = if (isGridView) StokrotkaGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (isGridView) "${pages.size} stron" else "${pagerState.currentPage + 1} z ${pages.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(onClick = { 
                        if (isGridView) {
                            data.linkUrl?.let { downloadPage(it, data.name, isPdf = true) }
                        } else {
                            val currentPage = pages.getOrNull(pagerState.currentPage)
                            currentPage?.let { 
                                downloadPage(it.imageUrl, "${data.name}_strona_${it.page}") 
                            }
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Download")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(pages) { page ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(page.page - 1)
                                    isGridView = false
                                }
                            }
                        ) {
                            Card(
                                modifier = Modifier.aspectRatio(0.7f),
                                shape = RoundedCornerShape(4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                AsyncImage(
                                    model = page.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = page.page.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) { pageIndex ->
                    val page = pages.getOrNull(pageIndex)
                    if (page != null) {
                        AsyncImage(
                            model = page.imageUrl,
                            contentDescription = "Page ${page.page}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}
