package com.example.ui.screens.settings

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BusinessSettings
import com.example.data.repository.BusinessRepository
import com.example.util.Localization
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: BusinessRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()

    var businessName by remember { mutableStateOf(settings.businessName) }
    var phone by remember { mutableStateOf(settings.phone) }
    var email by remember { mutableStateOf(settings.email) }
    var address by remember { mutableStateOf(settings.address) }
    var taxNumber by remember { mutableStateOf(settings.taxNumber) }
    var currencyCode by remember { mutableStateOf(settings.currencyCode) }
    var currencySymbol by remember { mutableStateOf(settings.currencySymbol) }
    var taxRate by remember { mutableStateOf(settings.taxRatePercent.toString()) }
    var invoiceNotes by remember { mutableStateOf(settings.invoiceNotes) }
    var allowNegativeStock by remember { mutableStateOf(settings.allowNegativeStock) }
    var language by remember { mutableStateOf(settings.language) }
    var securityPin by remember { mutableStateOf(settings.securityPin ?: "") }
    var requirePin by remember { mutableStateOf(settings.requirePinOnStart) }

    LaunchedEffect(settingsState) {
        if (settingsState != null) {
            val s = settingsState!!
            businessName = s.businessName
            phone = s.phone
            email = s.email
            address = s.address
            taxNumber = s.taxNumber
            currencyCode = s.currencyCode
            currencySymbol = s.currencySymbol
            taxRate = s.taxRatePercent.toString()
            invoiceNotes = s.invoiceNotes
            allowNegativeStock = s.allowNegativeStock
            language = s.language
            securityPin = s.securityPin ?: ""
            requirePin = s.requirePinOnStart
        }
    }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings & Localization", fontWeight = FontWeight.Bold) },
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
            // Language Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Application Language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val languages = listOf(
                            "en" to "English (US / International)",
                            "fr" to "Français (French)",
                            "ar" to "العربية (Arabic - RTL)"
                        )

                        var langExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = langExpanded,
                            onExpandedChange = { langExpanded = !langExpanded }
                        ) {
                            OutlinedTextField(
                                value = languages.firstOrNull { it.first == language }?.second ?: language,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Language") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = langExpanded,
                                onDismissRequest = { langExpanded = false }
                            ) {
                                languages.forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            language = code
                                            langExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Currency & Finance Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Currency & Taxation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = currencySymbol,
                                onValueChange = { currencySymbol = it },
                                label = { Text("Symbol (e.g. $, €, MAD)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currencyCode,
                                onValueChange = { currencyCode = it },
                                label = { Text("ISO Code (USD, EUR)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = taxRate,
                            onValueChange = { taxRate = it },
                            label = { Text("Default Sales Tax Rate (%)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Business Profile
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Business Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Company / Business Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Official Contact Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Official Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Commercial Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = taxNumber,
                            onValueChange = { taxNumber = it },
                            label = { Text("Tax Registration / VAT ID") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Inventory & Stock Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Inventory Policies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow Negative Inventory", fontWeight = FontWeight.SemiBold)
                                Text("Permits sales even if on-hand stock reaches zero", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = allowNegativeStock,
                                onCheckedChange = { allowNegativeStock = it }
                            )
                        }
                    }
                }
            }

            // Security PIN Lock
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Application Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Require PIN Lock", fontWeight = FontWeight.SemiBold)
                                Text("Protects sensitive business figures with passcode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = requirePin,
                                onCheckedChange = { requirePin = it }
                            )
                        }

                        if (requirePin) {
                            OutlinedTextField(
                                value = securityPin,
                                onValueChange = { securityPin = it },
                                label = { Text("4-Digit Security PIN") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val tax = taxRate.toDoubleOrNull() ?: 5.0
                        val updated = settings.copy(
                            businessName = businessName.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            taxNumber = taxNumber.trim(),
                            currencyCode = currencyCode.trim(),
                            currencySymbol = currencySymbol.trim(),
                            taxRatePercent = tax,
                            invoiceNotes = invoiceNotes.trim(),
                            allowNegativeStock = allowNegativeStock,
                            language = language,
                            securityPin = securityPin.trim(),
                            requirePinOnStart = requirePin
                        )
                        scope.launch {
                            repository.updateSettings(updated)
                            snackbarHostState.showSnackbar("Settings saved successfully!")
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save All Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
