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
import com.example.data.local.entity.Expense
import com.example.data.local.entity.ExpenseCategory
import com.example.data.repository.BusinessRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    var category by remember { mutableStateOf(ExpenseCategory.UTILITIES) }
    var amountInput by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableLongStateOf(1L) }

    Scaffold(
        modifier = modifier.testTag("add_expense_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Record Expense", fontWeight = FontWeight.Bold) },
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
            // Category Dropdown
            item {
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        value = category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        ExpenseCategory.values().forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    category = c
                                    catExpanded = false
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
                    label = { Text("Amount (${settings.currencySymbol}) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Account Dropdown
            item {
                var accExpanded by remember { mutableStateOf(false) }
                val currentAcc = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()
                ExposedDropdownMenuBox(
                    expanded = accExpanded,
                    onExpandedChange = { accExpanded = !accExpanded }
                ) {
                    OutlinedTextField(
                        value = currentAcc?.name ?: "Select Payment Source",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid From Account *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = accExpanded,
                        onDismissRequest = { accExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${CurrencyUtils.formatCents(acc.balanceCents, settings.currencySymbol, settings.currencyDecimals)})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Recipient / Payee
            item {
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Vendor / Recipient") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Save
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val amountCents = CurrencyUtils.parseToCents(amountInput, settings.currencyDecimals)
                        if (amountCents <= 0L) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a valid amount") }
                            return@Button
                        }

                        scope.launch {
                            repository.recordExpense(
                                Expense(
                                    category = category,
                                    amountCents = amountCents,
                                    financialAccountId = selectedAccountId,
                                    recipient = recipient.trim(),
                                    description = description.trim()
                                )
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Expense", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
