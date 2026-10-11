package com.example.ui.screens.finance

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Customer
import com.example.data.local.entity.Expense
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.Supplier
import com.example.data.repository.BusinessRepository
import com.example.ui.components.CashFlowForecastChart
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.Localization

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FinanceOverviewScreen(
    repository: BusinessRepository,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToCashClosing: () -> Unit,
    onNavigateToReceivePayment: () -> Unit,
    onNavigateToPaySupplier: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val expenses by repository.allExpenses.collectAsStateWithLifecycle(initialValue = emptyList())
    val customers by repository.allCustomers.collectAsStateWithLifecycle(initialValue = emptyList())
    val suppliers by repository.allSuppliers.collectAsStateWithLifecycle(initialValue = emptyList())
    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)

    val settings = settingsState ?: BusinessSettings()
    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals
    val lang = settings.language

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Accounts", "Expenses", "Receivables", "Payables")

    // Calculations
    val availableLiquidity = accounts.filter { !it.isRestricted }.sumOf { it.balanceCents }
    val restrictedFunds = accounts.filter { it.isRestricted }.sumOf { it.balanceCents }
    val totalCashAndBank = accounts.sumOf { it.balanceCents }

    val customerReceivables = customers.sumOf { it.currentBalanceCents.coerceAtLeast(0L) }
    val supplierPayables = suppliers.sumOf { it.currentBalanceCents.coerceAtLeast(0L) }
    val inventoryValuation = products.sumOf { (it.currentStock.coerceAtLeast(0.0) * it.costPriceCents).toLong() }

    // Current Assets - Current Liabilities = Working Capital
    val currentAssets = availableLiquidity + customerReceivables + inventoryValuation
    val currentLiabilities = supplierPayables
    val workingCapital = currentAssets - currentLiabilities

    // Cash flow forecast 30-day projection points
    val forecastPoints = listOf(
        availableLiquidity,
        availableLiquidity + (customerReceivables * 0.2).toLong() - (supplierPayables * 0.1).toLong(),
        availableLiquidity + (customerReceivables * 0.5).toLong() - (supplierPayables * 0.4).toLong(),
        availableLiquidity + (customerReceivables * 0.8).toLong() - (supplierPayables * 0.7).toLong(),
        availableLiquidity + customerReceivables - supplierPayables
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("finance_overview_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Localization.getString("finance", lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Liquidity, Working Capital & Accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onNavigateToTransfer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transfer")
                }
            }
        }

        // Quick action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToAddExpense,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expense")
            }
            OutlinedButton(
                onClick = onNavigateToCashClosing,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cash Close")
            }
            OutlinedButton(
                onClick = onNavigateToReceivePayment,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Collect")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTabIndex) {
                // ACCOUNTS TAB
                0 -> {
                    // Available Liquidity Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Available Unrestricted Liquidity",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyUtils.formatCents(availableLiquidity, sym, dec),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Transparent formula: Sum of unrestricted cash registers and bank accounts. Excludes ${CurrencyUtils.formatCents(restrictedFunds, sym, dec)} in escrow/restricted reserve.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Working Capital Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Working Capital Estimation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Current Assets:")
                                    Text(CurrencyUtils.formatCents(currentAssets, sym, dec), fontWeight = FontWeight.SemiBold)
                                }
                                Text("  • Liquidity: ${CurrencyUtils.formatCents(availableLiquidity, sym, dec)}", style = MaterialTheme.typography.bodySmall)
                                Text("  • Receivables: ${CurrencyUtils.formatCents(customerReceivables, sym, dec)}", style = MaterialTheme.typography.bodySmall)
                                Text("  • Stock (Cost): ${CurrencyUtils.formatCents(inventoryValuation, sym, dec)}", style = MaterialTheme.typography.bodySmall)

                                HorizontalDivider()

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Current Liabilities (Payables):")
                                    Text("-" + CurrencyUtils.formatCents(currentLiabilities, sym, dec), color = MaterialTheme.colorScheme.error)
                                }

                                HorizontalDivider()

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("ESTIMATED WORKING CAPITAL:", fontWeight = FontWeight.Bold)
                                    Text(CurrencyUtils.formatCents(workingCapital, sym, dec), fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                                }
                            }
                        }
                    }

                    // Accounts List
                    item {
                        Text("Active Accounts & Cash Registers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    items(accounts) { acc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (acc.isRestricted) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (acc.isRestricted) Icons.Default.Lock else if (acc.accountType == AccountType.BANK) Icons.Default.AccountBalance else Icons.Default.PointOfSale,
                                            contentDescription = null,
                                            tint = if (acc.isRestricted) Color(0xFFD97706) else EmeraldGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(acc.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${acc.accountType.name} ${if (acc.accountNumber.isNotBlank()) "• ${acc.accountNumber}" else ""}${if (acc.isRestricted) " • [RESTRICTED]" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = CurrencyUtils.formatCents(acc.balanceCents, sym, dec),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // 30-Day Forecast Chart
                    item {
                        CashFlowForecastChart(
                            title = "Cash Flow Forecast (30-day)",
                            points = forecastPoints,
                            currencySymbol = sym
                        )
                    }
                }

                // EXPENSES TAB
                1 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Expenses: ${CurrencyUtils.formatCents(expenses.sumOf { it.amountCents }, sym, dec)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(onClick = onNavigateToAddExpense, shape = RoundedCornerShape(10.dp)) {
                                Text("+ Add Expense")
                            }
                        }
                    }

                    if (expenses.isEmpty()) {
                        item {
                            Text("No expenses recorded yet.", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(expenses) { exp ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exp.category.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${exp.recipient} • ${DateUtils.formatDate(exp.expenseDate)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (exp.description.isNotBlank()) {
                                            Text(exp.description, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Text(
                                        text = "-" + CurrencyUtils.formatCents(exp.amountCents, sym, dec),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // RECEIVABLES TAB (Customer Debts)
                2 -> {
                    val indebtedCustomers = customers.filter { it.currentBalanceCents > 0 }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Receivables: ${CurrencyUtils.formatCents(customerReceivables, sym, dec)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(onClick = onNavigateToReceivePayment, shape = RoundedCornerShape(10.dp)) {
                                Text("Collect Payment")
                            }
                        }
                    }

                    if (indebtedCustomers.isEmpty()) {
                        item {
                            Text("All customer accounts are clear with zero debt!", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(indebtedCustomers) { cust ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cust.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Phone: ${cust.phone} • Limit: ${CurrencyUtils.formatCents(cust.creditLimitCents, sym, dec)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyUtils.formatCents(cust.currentBalanceCents, sym, dec),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text("Owed to you", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                // PAYABLES TAB (Supplier Debts)
                3 -> {
                    val indebtedSuppliers = suppliers.filter { it.currentBalanceCents > 0 }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Payables: ${CurrencyUtils.formatCents(supplierPayables, sym, dec)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = onNavigateToPaySupplier, shape = RoundedCornerShape(10.dp)) {
                                Text("Pay Supplier")
                            }
                        }
                    }

                    if (indebtedSuppliers.isEmpty()) {
                        item {
                            Text("All supplier accounts are paid in full with zero debt!", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(indebtedSuppliers) { sup ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sup.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Contact: ${sup.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyUtils.formatCents(sup.currentBalanceCents, sym, dec),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text("Owed to supplier", style = MaterialTheme.typography.labelSmall)
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
