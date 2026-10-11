package com.example.ui.screens.sales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.PaymentStatus
import com.example.data.repository.BusinessRepository
import com.example.ui.components.StatusBadge
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.Localization

@Composable
fun SalesListScreen(
    repository: BusinessRepository,
    onNavigateToNewSale: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit,
    onNavigateToReturns: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sales by repository.allSales.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)

    val settings = settingsState ?: BusinessSettings()
    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals
    val lang = settings.language

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<PaymentStatus?>(null) }

    val filteredSales = sales.filter { sale ->
        val matchesQuery = searchQuery.isBlank() ||
                sale.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                sale.customerName.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedFilter == null || sale.paymentStatus == selectedFilter
        matchesQuery && matchesStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("sales_list_screen")
    ) {
        // Top Bar & Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Localization.getString("sales", lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredSales.size} Invoices Recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToReturns,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AssignmentReturn, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text("Returns")
                }
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("sales_search_input"),
            placeholder = { Text(Localization.getString("search", lang) + " (Invoice #, Customer)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${sales.size})") }
                )
            }
            items(PaymentStatus.values()) { status ->
                val count = sales.count { it.paymentStatus == status }
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = if (selectedFilter == status) null else status },
                    label = { Text("${status.name} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Invoices List
        if (filteredSales.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No invoices found matching criteria",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSales) { sale ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToInvoice(sale.id) }
                            .testTag("sale_item_${sale.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sale.invoiceNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                StatusBadge(status = sale.paymentStatus)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = sale.customerName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = DateUtils.formatDate(sale.saleDate) + " • " + sale.paymentMethod.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyUtils.formatCents(sale.totalCents, sym, dec),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (sale.totalCents > sale.paidAmountCents) {
                                        Text(
                                            text = "Due: " + CurrencyUtils.formatCents(sale.totalCents - sale.paidAmountCents, sym, dec),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
