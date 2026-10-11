package com.example.ui.screens.reports

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import com.example.util.PdfExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sales by repository.allSales.collectAsStateWithLifecycle(initialValue = emptyList())
    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val expenses by repository.allExpenses.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals

    val totalSales = sales.sumOf { it.totalCents }
    val totalCogs = sales.sumOf { it.cogsCents }
    val grossProfit = totalSales - totalCogs
    val totalExpenses = expenses.sumOf { it.amountCents }
    val netProfit = grossProfit - totalExpenses
    val grossMarginPercent = if (totalSales > 0) (grossProfit * 100.0) / totalSales else 0.0

    val inventoryValuation = products.sumOf { (it.currentStock.coerceAtLeast(0.0) * it.costPriceCents).toLong() }

    Scaffold(
        modifier = modifier.testTag("reports_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Business Intelligence & Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val csvReport = buildString {
                                appendLine("SMARTBIZ FINANCIAL PERFORMANCE REPORT")
                                appendLine("Business: ${settings.businessName}")
                                appendLine("---------------------------------------")
                                appendLine("Total Sales: ${CurrencyUtils.formatCents(totalSales, sym, dec)}")
                                appendLine("Cost of Goods Sold (COGS): ${CurrencyUtils.formatCents(totalCogs, sym, dec)}")
                                appendLine("Gross Profit: ${CurrencyUtils.formatCents(grossProfit, sym, dec)} (${String.format("%.1f", grossMarginPercent)}%)")
                                appendLine("Operating Expenses: ${CurrencyUtils.formatCents(totalExpenses, sym, dec)}")
                                appendLine("Net Operating Profit: ${CurrencyUtils.formatCents(netProfit, sym, dec)}")
                                appendLine("Inventory Valuation (Cost): ${CurrencyUtils.formatCents(inventoryValuation, sym, dec)}")
                            }
                            PdfExportHelper.shareText(context, "SmartBiz Performance Report", csvReport)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profitability Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Income Statement & Margins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Sales Revenue:")
                            Text(CurrencyUtils.formatCents(totalSales, sym, dec), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cost of Goods Sold (COGS):")
                            Text("-" + CurrencyUtils.formatCents(totalCogs, sym, dec), color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GROSS PROFIT:", fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatCents(grossProfit, sym, dec), fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Text("Gross Margin: ${String.format("%.1f", grossMarginPercent)}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Operating Expenses:")
                            Text("-" + CurrencyUtils.formatCents(totalExpenses, sym, dec), color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NET OPERATING PROFIT:", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                CurrencyUtils.formatCents(netProfit, sym, dec),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (netProfit >= 0) EmeraldGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Inventory Valuation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Inventory & Asset Valuation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Weighted Average Cost Method: Reflects capital invested in physical stock on shelves and storage depots.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Stock Value (Cost):")
                            Text(CurrencyUtils.formatCents(inventoryValuation, sym, dec), fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Catalog Item Count:")
                            Text("${products.size} Products", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Export Button
            item {
                Button(
                    onClick = {
                        val report = buildString {
                            appendLine("SmartBiz Executive Financial Summary")
                            appendLine("Sales: ${CurrencyUtils.formatCents(totalSales, sym, dec)}")
                            appendLine("Gross Profit: ${CurrencyUtils.formatCents(grossProfit, sym, dec)}")
                            appendLine("Net Profit: ${CurrencyUtils.formatCents(netProfit, sym, dec)}")
                            appendLine("Inventory (Cost): ${CurrencyUtils.formatCents(inventoryValuation, sym, dec)}")
                        }
                        PdfExportHelper.shareText(context, "SmartBiz Executive Summary", report)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Export & Share Complete Report")
                }
            }
        }
    }
}
