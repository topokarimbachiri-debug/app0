package com.example.ui.screens.sales

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Sale
import com.example.data.local.entity.SaleItem
import com.example.data.repository.BusinessRepository
import com.example.ui.components.StatusBadge
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.PdfExportHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    saleId: Long,
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val sale by repository.getSale(saleId).collectAsStateWithLifecycle(initialValue = null)
    val items by repository.getSaleItems(saleId).collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    val sym = settings.currencySymbol
    val dec = settings.currencyDecimals

    var showReturnDialog by remember { mutableStateOf(false) }
    var returnReason by remember { mutableStateOf("Customer returned merchandise") }
    var restockChecked by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.testTag("invoice_detail_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(sale?.invoiceNumber ?: "Invoice Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (sale != null) {
                        IconButton(
                            onClick = {
                                val invoiceText = PdfExportHelper.generateInvoiceText(
                                    sale = sale!!,
                                    items = items,
                                    settings = settings,
                                    customer = null
                                )
                                PdfExportHelper.shareText(context, "Invoice ${sale!!.invoiceNumber}", invoiceText)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (sale == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Loading invoice...")
            }
        } else {
            val currentSale = sale!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentSale.invoiceNumber,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = DateUtils.formatDateTime(currentSale.saleDate),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = currentSale.paymentStatus)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Customer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentSale.customerName, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Payment Method", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentSale.paymentMethod.name, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Items list
                item {
                    Text(
                        text = "Purchased Items (${items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${item.quantity} × ${CurrencyUtils.formatCents(item.unitPriceCents, sym, dec)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                CurrencyUtils.formatCents(item.totalCents, sym, dec),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }

                // Financial Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:")
                                Text(CurrencyUtils.formatCents(currentSale.subtotalCents, sym, dec), fontWeight = FontWeight.SemiBold)
                            }
                            if (currentSale.discountCents > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:")
                                    Text("-" + CurrencyUtils.formatCents(currentSale.discountCents, sym, dec), color = MaterialTheme.colorScheme.error)
                                }
                            }
                            if (currentSale.taxCents > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tax:")
                                    Text("+" + CurrencyUtils.formatCents(currentSale.taxCents, sym, dec))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TOTAL INVOICE:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(CurrencyUtils.formatCents(currentSale.totalCents, sym, dec), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Paid:")
                                Text(CurrencyUtils.formatCents(currentSale.paidAmountCents, sym, dec), fontWeight = FontWeight.Bold)
                            }
                            val balanceDue = currentSale.totalCents - currentSale.paidAmountCents
                            if (balanceDue > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Balance Due (Receivable):", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                    Text(CurrencyUtils.formatCents(balanceDue, sym, dec), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Actions: Share & Returns
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val invoiceText = PdfExportHelper.generateInvoiceText(
                                    sale = currentSale,
                                    items = items,
                                    settings = settings,
                                    customer = null
                                )
                                PdfExportHelper.shareText(context, "Invoice ${currentSale.invoiceNumber}", invoiceText)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Print / Share")
                        }

                        if (!currentSale.isReturned) {
                            OutlinedButton(
                                onClick = { showReturnDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.AssignmentReturn, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("Process Return")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // Return Dialog
        if (showReturnDialog && sale != null) {
            Dialog(onDismissRequest = { showReturnDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Process Sales Return",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Refund amount: ${CurrencyUtils.formatCents(sale!!.paidAmountCents, sym, dec)}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = returnReason,
                            onValueChange = { returnReason = it },
                            label = { Text("Reason for Return") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = restockChecked,
                                onCheckedChange = { restockChecked = it }
                            )
                            Text("Return items back to warehouse stock")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showReturnDialog = false },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.processSalesReturn(
                                            saleId = sale!!.id,
                                            reason = returnReason,
                                            refundAmountCents = sale!!.paidAmountCents,
                                            isRestocked = restockChecked,
                                            financialAccountId = sale!!.financialAccountId
                                        )
                                        showReturnDialog = false
                                        snackbarHostState.showSnackbar("Return processed and recorded successfully")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Confirm Refund", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
