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
import com.example.data.repository.BusinessRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransferScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    var fromAccountId by remember { mutableLongStateOf(1L) }
    var toAccountId by remember { mutableLongStateOf(2L) }
    var amountInput by remember { mutableStateOf("") }
    var feeInput by remember { mutableStateOf("0") }
    var notesInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.testTag("account_transfer_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transfer Funds Between Accounts", fontWeight = FontWeight.Bold) },
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
                Text(
                    text = "Transferring balances between internal accounts does not affect net business profit or revenue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // From Account
            item {
                var fromExpanded by remember { mutableStateOf(false) }
                val fromAcc = accounts.firstOrNull { it.id == fromAccountId } ?: accounts.firstOrNull()
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded }
                ) {
                    OutlinedTextField(
                        value = "${fromAcc?.name ?: "Source Account"} (${CurrencyUtils.formatCents(fromAcc?.balanceCents ?: 0L, settings.currencySymbol, settings.currencyDecimals)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Source Account (Deduct From) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${CurrencyUtils.formatCents(acc.balanceCents, settings.currencySymbol, settings.currencyDecimals)})") },
                                onClick = {
                                    fromAccountId = acc.id
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // To Account
            item {
                var toExpanded by remember { mutableStateOf(false) }
                val toAcc = accounts.firstOrNull { it.id == toAccountId } ?: accounts.getOrNull(1) ?: accounts.firstOrNull()
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded }
                ) {
                    OutlinedTextField(
                        value = "${toAcc?.name ?: "Destination Account"} (${CurrencyUtils.formatCents(toAcc?.balanceCents ?: 0L, settings.currencySymbol, settings.currencyDecimals)})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Destination Account (Add To) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${CurrencyUtils.formatCents(acc.balanceCents, settings.currencySymbol, settings.currencyDecimals)})") },
                                onClick = {
                                    toAccountId = acc.id
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Amount
            item {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Transfer Amount (${settings.currencySymbol}) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Fee
            item {
                OutlinedTextField(
                    value = feeInput,
                    onValueChange = { feeInput = it },
                    label = { Text("Bank Transfer Fee (${settings.currencySymbol})") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Transfer Purpose / Reference") },
                    placeholder = { Text("e.g. End of day cash deposit to bank") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Confirm
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (fromAccountId == toAccountId) {
                            scope.launch { snackbarHostState.showSnackbar("Source and destination accounts must be different") }
                            return@Button
                        }
                        val amountCents = CurrencyUtils.parseToCents(amountInput, settings.currencyDecimals)
                        val feeCents = CurrencyUtils.parseToCents(feeInput, settings.currencyDecimals)
                        if (amountCents <= 0L) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid amount") }
                            return@Button
                        }

                        val fromAcc = accounts.firstOrNull { it.id == fromAccountId }
                        if (fromAcc != null && fromAcc.balanceCents < (amountCents + feeCents)) {
                            scope.launch { snackbarHostState.showSnackbar("Insufficient funds in ${fromAcc.name}") }
                            return@Button
                        }

                        scope.launch {
                            repository.transferFunds(
                                fromAccountId = fromAccountId,
                                toAccountId = toAccountId,
                                amountCents = amountCents,
                                feeCents = feeCents,
                                notes = notesInput.trim()
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Execute Transfer", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
