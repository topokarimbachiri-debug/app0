package com.example.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BusinessSettings
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashClosingScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val accounts by repository.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val cashAccounts = accounts.filter { it.accountType == AccountType.CASH }
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    var selectedAccountId by remember { mutableLongStateOf(cashAccounts.firstOrNull()?.id ?: 1L) }
    var physicalCountInput by remember { mutableStateOf("") }
    var closingNotes by remember { mutableStateOf("") }

    val activeAccount = cashAccounts.firstOrNull { it.id == selectedAccountId } ?: cashAccounts.firstOrNull()
    val systemExpectedCents = activeAccount?.balanceCents ?: 0L
    val countedCents = CurrencyUtils.parseToCents(physicalCountInput, settings.currencyDecimals)
    val diffCents = countedCents - systemExpectedCents

    Scaffold(
        modifier = modifier.testTag("cash_closing_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Daily Cash Closing & Reconciliation", fontWeight = FontWeight.Bold) },
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
            // Cash Register Selector
            item {
                var regExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = regExpanded,
                    onExpandedChange = { regExpanded = !regExpanded }
                ) {
                    OutlinedTextField(
                        value = activeAccount?.name ?: "Select Cash Register",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Register / Till *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = regExpanded,
                        onDismissRequest = { regExpanded = false }
                    ) {
                        cashAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${CurrencyUtils.formatCents(acc.balanceCents, settings.currencySymbol, settings.currencyDecimals)})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    regExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Expected System Balance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Expected Balance", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = CurrencyUtils.formatCents(systemExpectedCents, settings.currencySymbol, settings.currencyDecimals),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Recorded transactions, cash sales, and receipts for today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Physical Count Input
            item {
                OutlinedTextField(
                    value = physicalCountInput,
                    onValueChange = { physicalCountInput = it },
                    label = { Text("Actual Counted Cash In Drawer (${settings.currencySymbol}) *") },
                    placeholder = { Text("Enter physical count") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Discrepancy indicator if count entered
            if (physicalCountInput.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (diffCents == 0L) Color(0xFFD1FAE5) else if (diffCents > 0) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (diffCents == 0L) "Perfect Match!" else if (diffCents > 0) "Cash Over" else "Cash Short",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (diffCents == 0L) "Zero discrepancy recorded" else "Difference vs expected system balance",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "${if (diffCents > 0) "+" else ""}${CurrencyUtils.formatCents(diffCents, settings.currencySymbol, settings.currencyDecimals)}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (diffCents == 0L) EmeraldGreen else if (diffCents > 0) Color(0xFFD97706) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Notes
            item {
                OutlinedTextField(
                    value = closingNotes,
                    onValueChange = { closingNotes = it },
                    label = { Text("Reconciliation Notes / Cashier Sign-off") },
                    placeholder = { Text("e.g. Verified by shift supervisor") },
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
                        if (physicalCountInput.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter the actual cash counted") }
                            return@Button
                        }
                        scope.launch {
                            repository.recordCashClosing(
                                accountId = selectedAccountId,
                                actualCountCents = countedCents,
                                notes = closingNotes.trim()
                            )
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Confirm & Reconcile Register", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
