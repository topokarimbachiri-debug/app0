package com.example.ui.screens.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.local.entity.Supplier
import com.example.data.repository.BusinessRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val suppliers by repository.allSuppliers.collectAsStateWithLifecycle(initialValue = emptyList())
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    val filtered = suppliers.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Scaffold(
        modifier = modifier.testTag("suppliers_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Supplier Directory", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Supplier")
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search suppliers by name or contact...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { sup ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sup.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (sup.phone.isNotBlank()) {
                                    Text("Phone: ${sup.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyUtils.formatCents(sup.currentBalanceCents, settings.currencySymbol, settings.currencyDecimals),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (sup.currentBalanceCents > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (sup.currentBalanceCents > 0) "We Owe (Payable)" else "Paid in Full",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add New Supplier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Supplier / Vendor Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        scope.launch {
                                            repository.saveSupplier(
                                                Supplier(
                                                    name = newName.trim(),
                                                    phone = newPhone.trim(),
                                                    email = newEmail.trim()
                                                )
                                            )
                                            showAddDialog = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Supplier")
                            }
                        }
                    }
                }
            }
        }
    }
}
