package com.example.ui.screens.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Product
import com.example.data.repository.BusinessRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Long,
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categories by repository.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()
    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals

    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var costPriceInput by remember { mutableStateOf("") }
    var sellingPriceInput by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var minStockInput by remember { mutableStateOf("5") }
    var stockInput by remember { mutableStateOf("0") }
    var selectedCategoryId by remember { mutableLongStateOf(1L) }

    LaunchedEffect(productId) {
        if (productId > 0) {
            val existing = repository.getProductDirect(productId)
            if (existing != null) {
                name = existing.name
                sku = existing.sku
                barcode = existing.barcode
                description = existing.description
                costPriceInput = CurrencyUtils.centsToDecimalString(existing.costPriceCents, dec)
                sellingPriceInput = CurrencyUtils.centsToDecimalString(existing.sellingPriceCents, dec)
                unit = existing.unit
                minStockInput = existing.minStockLevel.toString()
                stockInput = existing.currentStock.toString()
                selectedCategoryId = existing.categoryId
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("add_edit_product_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (productId == 0L) "Add New Product" else "Edit Product", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Code") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceInput,
                        onValueChange = { costPriceInput = it },
                        label = { Text("Cost Price ($sym) *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = sellingPriceInput,
                        onValueChange = { sellingPriceInput = it },
                        label = { Text("Selling Price ($sym) *") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Current Stock") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = minStockInput,
                        onValueChange = { minStockInput = it },
                        label = { Text("Min Stock Alert") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            item {
                var unitExpanded by remember { mutableStateOf(false) }
                val units = listOf("pcs", "box", "kg", "liter", "meter", "pack")
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit of Measurement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unit = u
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a product name") }
                            return@Button
                        }
                        val cost = CurrencyUtils.parseToCents(costPriceInput, dec)
                        val price = CurrencyUtils.parseToCents(sellingPriceInput, dec)
                        val stock = stockInput.toDoubleOrNull() ?: 0.0
                        val minStock = minStockInput.toDoubleOrNull() ?: 5.0

                        val product = Product(
                            id = productId,
                            name = name.trim(),
                            sku = if (sku.isBlank()) "SKU-" + System.currentTimeMillis().toString().takeLast(5) else sku.trim(),
                            barcode = barcode.trim(),
                            categoryId = selectedCategoryId,
                            description = description.trim(),
                            costPriceCents = cost,
                            sellingPriceCents = price,
                            unit = unit,
                            minStockLevel = minStock,
                            currentStock = stock
                        )

                        scope.launch {
                            repository.saveProduct(product)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_product_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Product", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
