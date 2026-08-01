package io.github.zorinontop.twojastokrotka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zorinontop.twojastokrotka.models.FilterCriteria
import io.github.zorinontop.twojastokrotka.models.SortOption
import io.github.zorinontop.twojastokrotka.network.Offer
import io.github.zorinontop.twojastokrotka.ui.theme.StokrotkaGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterScreen(
    initialSort: SortOption,
    initialFilter: FilterCriteria,
    allOffers: List<Offer>,
    onApply: (SortOption, FilterCriteria) -> Unit,
    onBack: () -> Unit
) {
    var selectedSort by remember { mutableStateOf(initialSort) }
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var activeTab by remember { mutableStateOf(0) }

    val categories = remember(allOffers) {
        allOffers.mapNotNull { it.category }.distinct().sorted()
    }

    val petalOptions = listOf(5, 10, 15, 20, 25, 30, 35, 40)

    val filteredCount = remember(selectedFilter, allOffers) {
        allOffers.filter { offer ->
            val petalMatch = selectedFilter.selectedPetals.isEmpty() || selectedFilter.selectedPetals.contains(offer.pointCost)
            val categoryMatch = selectedFilter.selectedCategories.isEmpty() || selectedFilter.selectedCategories.contains(offer.category)
            petalMatch && categoryMatch
        }.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SORTUJ I FILTRUJ",
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
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedSort = SortOption.DOMYSLNE
                            selectedFilter = FilterCriteria()
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WYCZYŚĆ", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onApply(selectedSort, selectedFilter) },
                        modifier = Modifier.weight(1.2f).fillMaxHeight(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StokrotkaGreen)
                    ) {
                        Text(
                            text = "POKAŻ ($filteredCount)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = StokrotkaGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = StokrotkaGreen
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        val filterCount = selectedFilter.selectedPetals.size + selectedFilter.selectedCategories.size
                        Text(
                            text = "Filtrowanie ($filterCount)",
                            color = if (activeTab == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            text = "Sortowanie",
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (activeTab == 0) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        FilterHeader(
                            title = "Ilość wymaganych Płatków",
                            onClear = { selectedFilter = selectedFilter.copy(selectedPetals = emptySet()) },
                            count = selectedFilter.selectedPetals.size
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(petalOptions) { petals ->
                                val isSelected = selectedFilter.selectedPetals.contains(petals)
                                PetalFilterItem(
                                    petals = petals,
                                    isSelected = isSelected,
                                    onClick = {
                                        val newSet = if (isSelected) selectedFilter.selectedPetals - petals else selectedFilter.selectedPetals + petals
                                        selectedFilter = selectedFilter.copy(selectedPetals = newSet)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        FilterHeader(
                            title = "Kategorie",
                            onClear = { selectedFilter = selectedFilter.copy(selectedCategories = emptySet()) },
                            count = selectedFilter.selectedCategories.size
                        )
                    }

                    items(categories) { category ->
                        val isSelected = selectedFilter.selectedCategories.contains(category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = if (isSelected) selectedFilter.selectedCategories - category else selectedFilter.selectedCategories + category
                                    selectedFilter = selectedFilter.copy(selectedCategories = newSet)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(checkedColor = StokrotkaGreen)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = category.uppercase(), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(SortOption.values()) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSort = option }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSort == option,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = StokrotkaGreen)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = option.label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterHeader(title: String, count: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        if (count > 0) {
            Text(
                text = "WYCZYŚĆ ($count)",
                color = StokrotkaGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onClear() }
            )
        }
    }
}

@Composable
fun PetalFilterItem(petals: Int, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (isSelected) StokrotkaGreen else Color.LightGray),
        color = if (isSelected) StokrotkaGreen.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$petals",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) StokrotkaGreen else Color.White
            )
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) StokrotkaGreen else Color.White
            )
        }
    }
}
