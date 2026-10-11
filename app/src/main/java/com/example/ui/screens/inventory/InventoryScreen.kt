package com.example.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Product
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import com.example.util.Localization

@Composable
fun InventoryScreen(
    repository: BusinessRepository,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Long) -> Unit,
    onNavigateToStockAdjustment: () -> Unit,
    onNavigateToBarcodeScanner: () -> Unit,
    onNavigateToLedger: () -> Unit,
    onNavigateToWarehouses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)

    val settings = settingsState ?: BusinessSettings()
    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals
    val lang = settings.language

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var showOnlyLowStock by remember { mutableStateOf(false) }

    val totalValuation = products.sumOf { (it.currentStock.coerceAtLeast(0.0) * it.costPriceCents).toLong() }
    val lowStockCount = products.count { it.currentStock <= it.minStockLevel }

    val filteredProducts = products.filter { prod ->
        val matchesQuery = searchQuery.isBlank() ||
                prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.sku.contains(searchQuery, ignoreCase = true) ||
                prod.barcode.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || prod.categoryId == selectedCategoryId
        val matchesLowStock = !showOnlyLowStock || (prod.currentStock <= prod.minStockLevel)
        matchesQuery && matchesCategory && matchesLowStock
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Localization.getString("inventory", lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Valuation: ${CurrencyUtils.formatCents(totalValuation, sym, dec)} (Cost)",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToAddProduct,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }

        // Action Toolbar (Barcode Scanner, Stock Adjustment, Ledger, Warehouses)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = onNavigateToBarcodeScanner,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan")
                }
            }
            item {
                OutlinedButton(
                    onClick = onNavigateToStockAdjustment,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjust")
                }
            }
            item {
                OutlinedButton(
                    onClick = onNavigateToLedger,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ledger")
                }
            }
            item {
                OutlinedButton(
                    onClick = onNavigateToWarehouses,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Locations")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, SKU or barcode...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category & Low Stock Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null && !showOnlyLowStock,
                    onClick = {
                        selectedCategoryId = null
                        showOnlyLowStock = false
                    },
                    label = { Text("All (${products.size})") }
                )
            }
            if (lowStockCount > 0) {
                item {
                    FilterChip(
                        selected = showOnlyLowStock,
                        onClick = { showOnlyLowStock = !showOnlyLowStock },
                        label = { Text("Low Stock ($lowStockCount)") },
                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategoryId == cat.id,
                    onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                    label = { Text(cat.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Products List
        if (filteredProducts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No products found matching criteria",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { prod ->
                    val isLowStock = prod.currentStock <= prod.minStockLevel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEditProduct(prod.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = prod.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isLowStock) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = "LOW",
                                                color = Color(0xFFDC2626),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "SKU: ${prod.sku} • Cost: ${CurrencyUtils.formatCents(prod.costPriceCents, sym, dec)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Sell: ${CurrencyUtils.formatCents(prod.sellingPriceCents, sym, dec)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${prod.currentStock} ${prod.unit}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Min: ${prod.minStockLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
