package com.example.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Expense
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.Product
import com.example.data.local.entity.Sale
import com.example.data.local.entity.Supplier
import com.example.data.repository.BusinessRepository
import com.example.ui.components.ChartBarData
import com.example.ui.components.KpiCard
import com.example.ui.components.SalesBarChart
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.Localization

@Composable
fun DashboardScreen(
    repository: BusinessRepository,
    onNavigateToSales: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit,
    onNavigateToProduct: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val sales by repository.allSales.collectAsStateWithLifecycle(initialValue = emptyList())
    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val expenses by repository.allExpenses.collectAsStateWithLifecycle(initialValue = emptyList())
    val customers by repository.allCustomers.collectAsStateWithLifecycle(initialValue = emptyList())
    val suppliers by repository.allSuppliers.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)

    val settings = settingsState ?: BusinessSettings()
    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals
    val lang = settings.language

    // 1. Calculations
    val todayStart = DateUtils.getStartOfDay()
    val todaySalesList = sales.filter { it.saleDate >= todayStart }
    val todaySalesTotal = todaySalesList.sumOf { it.totalCents }

    // Total sales revenue and COGS
    val totalRevenue = sales.sumOf { it.totalCents }
    val totalCogs = sales.sumOf { it.cogsCents }
    val grossProfit = totalRevenue - totalCogs

    val totalExpenses = expenses.sumOf { it.amountCents }
    val netOperatingProfit = grossProfit - totalExpenses

    // Liquidity: sum of non-restricted accounts
    val availableLiquidity = accounts.filter { !it.isRestricted }.sumOf { it.balanceCents }
    val cashBalances = accounts.filter { it.accountType == AccountType.CASH && !it.isRestricted }.sumOf { it.balanceCents }
    val bankBalances = accounts.filter { it.accountType == AccountType.BANK && !it.isRestricted }.sumOf { it.balanceCents }

    // Inventory value at cost
    val inventoryValue = products.sumOf { (it.currentStock.coerceAtLeast(0.0) * it.costPriceCents).toLong() }
    val lowStockCount = products.count { it.currentStock <= it.minStockLevel }

    // Debts
    val totalReceivables = customers.sumOf { it.currentBalanceCents.coerceAtLeast(0L) }
    val totalPayables = suppliers.sumOf { it.currentBalanceCents.coerceAtLeast(0L) }

    // Working Capital = Current Assets (Liquidity + Receivables + Inventory) - Current Liabilities (Payables)
    val workingCapital = (availableLiquidity + totalReceivables + inventoryValue) - totalPayables

    // Weekly chart data
    val dayMs = 86400000L
    val chartData = (6 downTo 0).map { daysAgo ->
        val dayTime = System.currentTimeMillis() - (daysAgo * dayMs)
        val start = DateUtils.getStartOfDay(dayTime)
        val end = start + dayMs
        val daySales = sales.filter { it.saleDate in start until end }
        val dayRevenue = daySales.sumOf { it.totalCents }
        val dayProfit = daySales.sumOf { it.totalCents - it.cogsCents }
        val label = DateUtils.formatShortDate(dayTime).take(5)
        ChartBarData(label = label, value = dayRevenue, profit = dayProfit)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Greeting
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = settings.businessName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Localization.getString("dashboard", lang) + " • " + DateUtils.formatDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Available Liquidity Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToFinance() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Localization.getString("available_liquidity", lang),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Liquid Funds",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = CurrencyUtils.formatCents(availableLiquidity, sym, dec),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = Localization.getString("cash_in_hand", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = CurrencyUtils.formatCents(cashBalances, sym, dec),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = Localization.getString("bank_balances", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = CurrencyUtils.formatCents(bankBalances, sym, dec),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Two-column Primary KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = Localization.getString("today_sales", lang),
                    amount = CurrencyUtils.formatCents(todaySalesTotal, sym, dec),
                    subtitle = "${todaySalesList.size} orders",
                    icon = Icons.Default.Payments,
                    iconTint = EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSales
                )
                KpiCard(
                    title = Localization.getString("gross_profit", lang),
                    amount = CurrencyUtils.formatCents(grossProfit, sym, dec),
                    subtitle = "Margin: ${if (totalRevenue > 0) ((grossProfit * 100) / totalRevenue) else 0}%",
                    icon = Icons.Default.ShowChart,
                    iconTint = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFinance
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = Localization.getString("net_profit", lang),
                    amount = CurrencyUtils.formatCents(netOperatingProfit, sym, dec),
                    subtitle = "After ${CurrencyUtils.formatCents(totalExpenses, sym, dec)} exp",
                    icon = Icons.Default.AttachMoney,
                    iconTint = if (netOperatingProfit >= 0) EmeraldGreen else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFinance
                )
                KpiCard(
                    title = Localization.getString("inventory_valuation", lang),
                    amount = CurrencyUtils.formatCents(inventoryValue, sym, dec),
                    subtitle = "${products.size} SKU items",
                    icon = Icons.Default.Inventory2,
                    iconTint = AmberAccent,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventory
                )
            }
        }

        // Receivables & Payables Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = Localization.getString("receivables", lang),
                    amount = CurrencyUtils.formatCents(totalReceivables, sym, dec),
                    subtitle = "Customer Debt",
                    icon = Icons.Default.AccountBalance,
                    iconTint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFinance
                )
                KpiCard(
                    title = Localization.getString("payables", lang),
                    amount = CurrencyUtils.formatCents(totalPayables, sym, dec),
                    subtitle = "Supplier Debt",
                    icon = Icons.Default.Warning,
                    iconTint = if (totalPayables > 0) MaterialTheme.colorScheme.error else EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFinance
                )
            }
        }

        // Low stock alerts if any
        if (lowStockCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInventory() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$lowStockCount Products Below Minimum Stock",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Action required to avoid stockouts. Tap to view items.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF991B1B)
                        )
                    }
                }
            }
        }

        // Sales & Profit Chart
        item {
            SalesBarChart(
                title = "Sales & Profit (Last 7 Days)",
                data = chartData,
                currencySymbol = sym
            )
        }

        // Recent Invoices section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToSales() }
                )
            }
        }

        // Recent Sales Items
        val recentSales = sales.take(5)
        if (recentSales.isEmpty()) {
            item {
                Text(
                    text = "No sales recorded yet. Tap + to create your first sale.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(recentSales) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToInvoice(sale.id) },
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
                        Column {
                            Text(
                                text = sale.customerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sale.invoiceNumber} • ${DateUtils.formatDate(sale.saleDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = CurrencyUtils.formatCents(sale.totalCents, sym, dec),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusBadge(status = sale.paymentStatus)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
