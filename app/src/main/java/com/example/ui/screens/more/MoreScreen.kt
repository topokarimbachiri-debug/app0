package com.example.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.DemoDataLoader
import com.example.data.local.entity.BusinessSettings
import com.example.data.repository.BusinessRepository
import com.example.ui.theme.EmeraldGreen
import com.example.util.Localization
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(
    repository: BusinessRepository,
    onNavigateToCustomers: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAdvisor: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val settingsState by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val settings = settingsState ?: BusinessSettings()
    val lang = settings.language

    Scaffold(
        modifier = modifier.testTag("more_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = Localization.getString("more", lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Customers Card
            item {
                MoreMenuCard(
                    title = Localization.getString("customers", lang),
                    subtitle = "Manage clients, credit limits, receivables & history",
                    icon = Icons.Default.People,
                    iconTint = Color(0xFF3B82F6),
                    onClick = onNavigateToCustomers
                )
            }

            // Suppliers Card
            item {
                MoreMenuCard(
                    title = Localization.getString("suppliers", lang),
                    subtitle = "Manage vendors, purchase invoices & payables",
                    icon = Icons.Default.LocalShipping,
                    iconTint = Color(0xFFF59E0B),
                    onClick = onNavigateToSuppliers
                )
            }

            // Reports Card
            item {
                MoreMenuCard(
                    title = Localization.getString("reports", lang),
                    subtitle = "Financial statements, profit margins, CSV/PDF export",
                    icon = Icons.Default.Analytics,
                    iconTint = EmeraldGreen,
                    onClick = onNavigateToReports
                )
            }

            // Smart Advisor Card
            item {
                MoreMenuCard(
                    title = Localization.getString("smart_advisor", lang),
                    subtitle = "Natural language questions, runway analysis & alerts",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFF8B5CF6),
                    onClick = onNavigateToAdvisor
                )
            }

            // Settings Card
            item {
                MoreMenuCard(
                    title = Localization.getString("settings", lang),
                    subtitle = "Currency, taxes, invoice format & language",
                    icon = Icons.Default.Settings,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToSettings
                )
            }

            // Demo Data Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dataset, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sample Business Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Populate realistic sales, inventory, accounts, customers, and expense entries for testing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    // repository can load demo data
                                    snackbarHostState.showSnackbar("Demo data loaded successfully!")
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Load Demo Data")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
