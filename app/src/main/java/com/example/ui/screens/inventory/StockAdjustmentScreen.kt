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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Product
import com.example.data.local.entity.StockMovementType
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val products by repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var adjustmentType by remember { mutableStateOf(StockMovementType.PHYSICAL_COUNT) }
    var newQuantityInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("stock_adjustment_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Stock Adjustment", fontWeight = FontWeight.Bold) },
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
            // Product Dropdown
            item {
                var productExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = !productExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "Select Product",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} (In stock: ${prod.currentStock})") },
                                onClick = {
                                    selectedProduct = prod
                                    newQuantityInput = prod.currentStock.toString()
                                    productExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedProduct != null) {
                item {
                    Text(
                        text = "Current Stock Level: ${selectedProduct!!.currentStock} ${selectedProduct!!.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Adjustment Type Dropdown
            item {
                var typeExpanded by remember { mutableStateOf(false) }
                val types = listOf(
                    StockMovementType.PHYSICAL_COUNT to "Physical Count Reconciliation",
                    StockMovementType.ADJUSTMENT_ADD to "Stock Addition (Found)",
                    StockMovementType.ADJUSTMENT_SUB to "Stock Subtraction (Discrepancy)",
                    StockMovementType.DAMAGED to "Damaged / Spoiled Items"
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = types.firstOrNull { it.first == adjustmentType }?.second ?: adjustmentType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Adjustment Action") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    adjustmentType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // New Quantity
            item {
                OutlinedTextField(
                    value = newQuantityInput,
                    onValueChange = { newQuantityInput = it },
                    label = { Text(if (adjustmentType == StockMovementType.PHYSICAL_COUNT) "Reconciled Total Count *" else "Quantity to Adjust *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Reason
            item {
                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Reason / Audit Notes *") },
                    placeholder = { Text("e.g. End of month physical audit, broken pallet") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Submit
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val prod = selectedProduct
                        if (prod == null) {
                            scope.launch { snackbarHostState.showSnackbar("Please select a product") }
                            return@Button
                        }
                        val qty = newQuantityInput.toDoubleOrNull()
                        if (qty == null) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid quantity") }
                            return@Button
                        }

                        val targetStock = when (adjustmentType) {
                            StockMovementType.PHYSICAL_COUNT -> qty
                            StockMovementType.ADJUSTMENT_ADD -> prod.currentStock + qty
                            StockMovementType.ADJUSTMENT_SUB, StockMovementType.DAMAGED -> (prod.currentStock - qty).coerceAtLeast(0.0)
                            else -> qty
                        }

                        scope.launch {
                            repository.adjustStock(
                                productId = prod.id,
                                newStock = targetStock,
                                type = adjustmentType,
                                reason = if (reasonInput.isBlank()) "Stock adjustment" else reasonInput.trim()
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Record Stock Adjustment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
