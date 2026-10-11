package com.example.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.entity.Customer
import com.example.data.local.entity.FinancialAccount
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.PaymentStatus
import com.example.data.local.entity.Product
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    onSaleCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val customers by repository.allCustomers.collectAsStateWithLifecycle(initialValue = emptyList())
    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)

    val settings = settingsState ?: BusinessSettings()
    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cart state
    val cartItems = remember { mutableStateListOf<BusinessRepository.SaleCartItem>() }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var walkInCustomerName by remember { mutableStateOf("Walk-in Customer") }
    var discountInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedPaymentStatus by remember { mutableStateOf(PaymentStatus.PAID) }
    var selectedAccountId by remember { mutableLongStateOf(1L) }
    var paidAmountInput by remember { mutableStateOf("") }

    // Product search dialog/dropdown state
    var productSearchQuery by remember { mutableStateOf("") }
    var showProductPicker by remember { mutableStateOf(false) }

    // Calculations
    val subtotal = cartItems.sumOf { (it.quantity * it.unitPriceCents).toLong() - it.discountCents }
    val discountCents = CurrencyUtils.parseToCents(discountInput, dec)
    val taxableAmount = (subtotal - discountCents).coerceAtLeast(0L)
    val taxCents = ((taxableAmount * settings.taxRatePercent) / 100.0).toLong()
    val totalCents = taxableAmount + taxCents

    // Auto default paid amount
    val paidAmountCents = when (selectedPaymentStatus) {
        PaymentStatus.PAID -> totalCents
        PaymentStatus.UNPAID -> 0L
        else -> CurrencyUtils.parseToCents(paidAmountInput, dec)
    }

    Scaffold(
        modifier = modifier.testTag("new_sale_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Sale / POS", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Customer Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        var customerDropdownExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = customerDropdownExpanded,
                            onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: walkInCustomerName,
                                onValueChange = {
                                    walkInCustomerName = it
                                    selectedCustomer = null
                                },
                                label = { Text("Customer") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = customerDropdownExpanded,
                                onDismissRequest = { customerDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Walk-in Customer (Cash)") },
                                    onClick = {
                                        selectedCustomer = null
                                        walkInCustomerName = "Walk-in Customer"
                                        customerDropdownExpanded = false
                                    }
                                )
                                customers.forEach { cust ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cust.name, fontWeight = FontWeight.SemiBold)
                                                if (cust.currentBalanceCents > 0) {
                                                    Text(
                                                        "Debt: " + CurrencyUtils.formatCents(cust.currentBalanceCents, sym, dec),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedCustomer = cust
                                            walkInCustomerName = cust.name
                                            customerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Products in Cart
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cart Items (${cartItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showProductPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_item_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item")
                    }
                }
            }

            if (cartItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items in cart. Tap '+ Add Item' to select products.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(cartItems) { item ->
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
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${CurrencyUtils.formatCents(item.unitPriceCents, sym, dec)} each • Stock: ${item.product.currentStock}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Line: " + CurrencyUtils.formatCents((item.quantity * item.unitPriceCents).toLong(), sym, dec),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val idx = cartItems.indexOf(item)
                                        if (item.quantity > 1.0) {
                                            cartItems[idx] = item.copy(quantity = item.quantity - 1.0)
                                        } else {
                                            cartItems.removeAt(idx)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val idx = cartItems.indexOf(item)
                                        cartItems[idx] = item.copy(quantity = item.quantity + 1.0)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                                IconButton(
                                    onClick = { cartItems.remove(item) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            // Payment & Financial Account
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Payment Method & Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Payment Status dropdown
                        var statusExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPaymentStatus.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Payment Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("PAID (Fully Collected)") },
                                    onClick = {
                                        selectedPaymentStatus = PaymentStatus.PAID
                                        statusExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("PARTIALLY PAID") },
                                    onClick = {
                                        selectedPaymentStatus = PaymentStatus.PARTIALLY_PAID
                                        statusExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("UNPAID (Full Credit / Receivables)") },
                                    onClick = {
                                        selectedPaymentStatus = PaymentStatus.UNPAID
                                        statusExpanded = false
                                    }
                                )
                            }
                        }

                        // Payment Method dropdown
                        var methodExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = methodExpanded,
                            onExpandedChange = { methodExpanded = !methodExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedPaymentMethod.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Payment Method") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = methodExpanded,
                                onDismissRequest = { methodExpanded = false }
                            ) {
                                PaymentMethod.values().forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.name) },
                                        onClick = {
                                            selectedPaymentMethod = method
                                            methodExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Financial Account (destination for collected cash)
                        var accountExpanded by remember { mutableStateOf(false) }
                        val currentAccount = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()
                        ExposedDropdownMenuBox(
                            expanded = accountExpanded,
                            onExpandedChange = { accountExpanded = !accountExpanded }
                        ) {
                            OutlinedTextField(
                                value = currentAccount?.name ?: "Main Cash Register",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Deposit Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false }
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${CurrencyUtils.formatCents(acc.balanceCents, sym, dec)})") },
                                        onClick = {
                                            selectedAccountId = acc.id
                                            accountExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Partial amount input if partial
                        if (selectedPaymentStatus == PaymentStatus.PARTIALLY_PAID) {
                            OutlinedTextField(
                                value = paidAmountInput,
                                onValueChange = { paidAmountInput = it },
                                label = { Text("Amount Paid Now ($sym)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Discount input
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Invoice Discount ($sym)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Summary Totals
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:")
                            Text(CurrencyUtils.formatCents(subtotal, sym, dec), fontWeight = FontWeight.SemiBold)
                        }
                        if (discountCents > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount:")
                                Text("-" + CurrencyUtils.formatCents(discountCents, sym, dec), color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax (${settings.taxRatePercent}%):")
                            Text("+" + CurrencyUtils.formatCents(taxCents, sym, dec))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(CurrencyUtils.formatCents(totalCents, sym, dec), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Paid Amount:")
                            Text(CurrencyUtils.formatCents(paidAmountCents, sym, dec), fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                        val balanceDue = (totalCents - paidAmountCents).coerceAtLeast(0L)
                        if (balanceDue > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Remaining Debt:")
                                Text(CurrencyUtils.formatCents(balanceDue, sym, dec), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Confirm & Complete Button
            item {
                Button(
                    onClick = {
                        if (cartItems.isEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar("Cart is empty! Please add products.") }
                            return@Button
                        }

                        // Validate negative stock if disallowed
                        if (!settings.allowNegativeStock) {
                            val insufficient = cartItems.filter { it.quantity > it.product.currentStock }
                            if (insufficient.isNotEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Insufficient stock for: ${insufficient.first().product.name} (Available: ${insufficient.first().product.currentStock})")
                                }
                                return@Button
                            }
                        }

                        scope.launch {
                            try {
                                val saleId = repository.createSale(
                                    customerId = selectedCustomer?.id,
                                    customerName = selectedCustomer?.name ?: walkInCustomerName,
                                    cartItems = cartItems.toList(),
                                    discountCents = discountCents,
                                    taxRatePercent = settings.taxRatePercent,
                                    paymentStatus = selectedPaymentStatus,
                                    paymentMethod = selectedPaymentMethod,
                                    paidAmountCents = paidAmountCents,
                                    financialAccountId = if (paidAmountCents > 0) selectedAccountId else null
                                )
                                onSaleCreated(saleId)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error completing sale: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("complete_sale_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text(
                        text = "Complete Sale (${CurrencyUtils.formatCents(totalCents, sym, dec)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Product Picker Dialog
        if (showProductPicker) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showProductPicker = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = productSearchQuery,
                            onValueChange = { productSearchQuery = it },
                            placeholder = { Text("Search by name, SKU or barcode") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val filtered = products.filter {
                            productSearchQuery.isBlank() ||
                                    it.name.contains(productSearchQuery, ignoreCase = true) ||
                                    it.sku.contains(productSearchQuery, ignoreCase = true) ||
                                    it.barcode.contains(productSearchQuery, ignoreCase = true)
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { prod ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val existing = cartItems.find { it.product.id == prod.id }
                                            if (existing != null) {
                                                val idx = cartItems.indexOf(existing)
                                                cartItems[idx] = existing.copy(quantity = existing.quantity + 1.0)
                                            } else {
                                                cartItems.add(
                                                    BusinessRepository.SaleCartItem(
                                                        product = prod,
                                                        quantity = 1.0,
                                                        unitPriceCents = prod.sellingPriceCents
                                                    )
                                                )
                                            }
                                            showProductPicker = false
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(prod.name, fontWeight = FontWeight.Bold)
                                            Text("SKU: ${prod.sku} • Stock: ${prod.currentStock} ${prod.unit}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Text(
                                            CurrencyUtils.formatCents(prod.sellingPriceCents, sym, dec),
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
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
