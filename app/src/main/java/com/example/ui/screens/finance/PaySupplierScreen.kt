package com.example.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.Supplier
import com.example.data.repository.BusinessRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaySupplierScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val suppliers by repository.allSuppliers.collectAsStateWithLifecycle(initialValue = emptyList())
    val indebtedSuppliers = suppliers.filter { it.currentBalanceCents > 0 }
    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    var selectedSupplier by remember { mutableStateOf<Supplier?>(indebtedSuppliers.firstOrNull() ?: suppliers.firstOrNull()) }
    var amountInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    var selectedAccountId by remember { mutableLongStateOf(3L) }
    var referenceInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("pay_supplier_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pay Supplier (Settle Debt)", fontWeight = FontWeight.Bold) },
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
            // Supplier Selector
            item {
                var supExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = supExpanded,
                    onExpandedChange = { supExpanded = !supExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedSupplier?.name ?: "Select Supplier"} (Debt: ${CurrencyUtils.formatCents(selectedSupplier?.currentBalanceCents ?: 0L, settings.currencySymbol, settings.currencyDecimals)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Supplier *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = supExpanded,
                        onDismissRequest = { supExpanded = false }
                    ) {
                        suppliers.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} (Debt: ${CurrencyUtils.formatCents(s.currentBalanceCents, settings.currencySymbol, settings.currencyDecimals)})") },
                                onClick = {
                                    selectedSupplier = s
                                    amountInput = CurrencyUtils.centsToDecimalString(s.currentBalanceCents, settings.currencyDecimals)
                                    supExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Amount Paid
            item {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Payment Amount (${settings.currencySymbol}) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Payment Method
            item {
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
                        PaymentMethod.values().forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    selectedPaymentMethod = m
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Source Account
            item {
                var accExpanded by remember { mutableStateOf(false) }
                val currentAcc = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()
                ExposedDropdownMenuBox(
                    expanded = accExpanded,
                    onExpandedChange = { accExpanded = !accExpanded }
                ) {
                    OutlinedTextField(
                        value = currentAcc?.name ?: "Select Payment Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pay From Account *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accExpanded,
                        onDismissRequest = { accExpanded = false }
                    ) {
                        accounts.forEach { a ->
                            DropdownMenuItem(
                                text = { Text("${a.name} (${CurrencyUtils.formatCents(a.balanceCents, settings.currencySymbol, settings.currencyDecimals)})") },
                                onClick = {
                                    selectedAccountId = a.id
                                    accExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Reference
            item {
                OutlinedTextField(
                    value = referenceInput,
                    onValueChange = { referenceInput = it },
                    label = { Text("Bank Transfer / Cheque Reference #") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Payment Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Submit
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val sup = selectedSupplier
                        if (sup == null) {
                            scope.launch { snackbarHostState.showSnackbar("Please select a supplier") }
                            return@Button
                        }
                        val amountCents = CurrencyUtils.parseToCents(amountInput, settings.currencyDecimals)
                        if (amountCents <= 0L) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid amount") }
                            return@Button
                        }

                        val sourceAcc = accounts.firstOrNull { it.id == selectedAccountId }
                        if (sourceAcc != null && sourceAcc.balanceCents < amountCents) {
                            scope.launch { snackbarHostState.showSnackbar("Insufficient funds in ${sourceAcc.name}") }
                            return@Button
                        }

                        scope.launch {
                            repository.paySupplier(
                                supplierId = sup.id,
                                purchaseId = null,
                                amountCents = amountCents,
                                paymentMethod = selectedPaymentMethod,
                                financialAccountId = selectedAccountId,
                                reference = referenceInput.trim(),
                                notes = notesInput.trim()
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Execute Supplier Settlement", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
