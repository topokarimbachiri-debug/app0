package com.example.ui.screens.smart

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils

data class AdvisorMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAdvisorScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sales by repository.allSales.collectAsStateWithLifecycle(initialValue = emptyList())
    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val expenses by repository.allExpenses.collectAsStateWithLifecycle(initialValue = emptyList())
    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val customers by repository.allCustomers.collectAsStateWithLifecycle(initialValue = emptyList())
    val suppliers by repository.allSuppliers.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals

    var userInput by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            AdvisorMessage(
                isUser = false,
                text = "Hello! I am your SmartBiz Financial & Inventory Advisor. I analyze your sales, liquidity, receivables, and warehouse levels to provide actionable business intelligence."
            )
        )
    }

    // Proactive insights
    val availableLiquidity = accounts.filter { !it.isRestricted }.sumOf { it.balanceCents }
    val lowStockItems = products.filter { it.currentStock <= it.minStockLevel }
    val totalCustomerDebt = customers.sumOf { it.currentBalanceCents.coerceAtLeast(0L) }
    val totalSupplierDebt = suppliers.sumOf { it.currentBalanceCents.coerceAtLeast(0L) }

    fun processQuery(query: String) {
        val q = query.lowercase().trim()
        val response = when {
            q.contains("profit") -> {
                val rev = sales.sumOf { it.totalCents }
                val cogs = sales.sumOf { it.cogsCents }
                val exp = expenses.sumOf { it.amountCents }
                val gross = rev - cogs
                val net = gross - exp
                "Your Total Gross Profit is ${CurrencyUtils.formatCents(gross, sym, dec)} (Sales: ${CurrencyUtils.formatCents(rev, sym, dec)} minus COGS: ${CurrencyUtils.formatCents(cogs, sym, dec)}). After ${CurrencyUtils.formatCents(exp, sym, dec)} in operating expenses, your Net Operating Profit is ${CurrencyUtils.formatCents(net, sym, dec)}."
            }
            q.contains("stock") || q.contains("reorder") || q.contains("low") -> {
                if (lowStockItems.isEmpty()) {
                    "All inventory levels are healthy! No items are currently below their minimum safety stock threshold."
                } else {
                    val itemsSummary = lowStockItems.joinToString(", ") { "${it.name} (${it.currentStock}/${it.minStockLevel} ${it.unit})" }
                    "You have ${lowStockItems.size} item(s) requiring immediate replenishment: $itemsSummary. Suggested reorder: Purchase at least 2x minimum threshold to maintain safety buffer."
                }
            }
            q.contains("liquidity") || q.contains("cash") || q.contains("runway") -> {
                val monthlyExp = expenses.sumOf { it.amountCents }.coerceAtLeast(1L)
                val monthsRunway = if (monthlyExp > 0) (availableLiquidity.toDouble() / monthlyExp.toDouble()) else 99.0
                "Available Unrestricted Liquidity is ${CurrencyUtils.formatCents(availableLiquidity, sym, dec)}. Based on recent operating expenses, you have approximately ${String.format("%.1f", monthsRunway)} months of cash runway."
            }
            q.contains("debt") || q.contains("receivable") || q.contains("owe") -> {
                val indebtedCust = customers.filter { it.currentBalanceCents > 0 }
                "Customers owe you ${CurrencyUtils.formatCents(totalCustomerDebt, sym, dec)} across ${indebtedCust.size} accounts. You owe suppliers ${CurrencyUtils.formatCents(totalSupplierDebt, sym, dec)} in payables."
            }
            else -> {
                "Based on current data: Liquidity is ${CurrencyUtils.formatCents(availableLiquidity, sym, dec)}, ${lowStockItems.size} products are low in stock, and customer receivables stand at ${CurrencyUtils.formatCents(totalCustomerDebt, sym, dec)}. You can ask about 'profit', 'low stock', 'liquidity', or 'customer debt'."
            }
        }
        chatMessages.add(AdvisorMessage(isUser = true, text = query))
        chatMessages.add(AdvisorMessage(isUser = false, text = response))
    }

    Scaffold(
        modifier = modifier.testTag("smart_advisor_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SmartBiz Advisor", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Proactive Business Alerts
            if (lowStockItems.isNotEmpty() || totalSupplierDebt > availableLiquidity) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Automated Intelligence Alert", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            if (lowStockItems.isNotEmpty()) {
                                Text("${lowStockItems.size} products below threshold require purchase orders.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF78350F))
                            }
                        }
                    }
                }
            }

            // Quick Question Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "How much profit did I make?",
                    "Which products should I reorder?",
                    "What is my available liquidity?",
                    "Who owes me money?"
                )
                items(suggestions) { sugg ->
                    AssistChip(
                        onClick = { processQuery(sugg) },
                        label = { Text(sugg) },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat Message Thread
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Message Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Ask a business question...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            processQuery(userInput)
                            userInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
