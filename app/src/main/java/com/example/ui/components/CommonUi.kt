package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentStatus
import com.example.ui.theme.EmeraldGreen

@Composable
fun KpiCard(
    title: String,
    amount: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color = EmeraldGreen,
    trendPercent: Double? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null || trendPercent != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trendPercent != null) {
                        val isPositive = trendPercent >= 0
                        Icon(
                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) EmeraldGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.1f", trendPercent)}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPositive) EmeraldGreen else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        PaymentStatus.PAID -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "PAID")
        PaymentStatus.PARTIALLY_PAID -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "PARTIAL")
        PaymentStatus.UNPAID -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "UNPAID")
        PaymentStatus.OVERDUE -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "OVERDUE")
        PaymentStatus.REFUNDED -> Triple(Color(0xFFE2E8F0), Color(0xFF475569), "REFUNDED")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun FloatingSpeedDial(
    onNewSale: () -> Unit,
    onAddProduct: () -> Unit,
    onAddExpense: () -> Unit,
    onReceivePayment: () -> Unit,
    onStockAdjustment: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedDialItem(
                    label = "Stock Adjustment",
                    icon = Icons.Default.Inventory,
                    tint = Color(0xFF8B5CF6),
                    onClick = {
                        expanded = false
                        onStockAdjustment()
                    }
                )
                SpeedDialItem(
                    label = "Receive Payment",
                    icon = Icons.Default.Payments,
                    tint = EmeraldGreen,
                    onClick = {
                        expanded = false
                        onReceivePayment()
                    }
                )
                SpeedDialItem(
                    label = "Record Expense",
                    icon = Icons.Default.AttachMoney,
                    tint = Color(0xFFEF4444),
                    onClick = {
                        expanded = false
                        onAddExpense()
                    }
                )
                SpeedDialItem(
                    label = "Add Product",
                    icon = Icons.Default.Add,
                    tint = Color(0xFF3B82F6),
                    onClick = {
                        expanded = false
                        onAddProduct()
                    }
                )
                SpeedDialItem(
                    label = "New Sale / POS",
                    icon = Icons.Default.PointOfSale,
                    tint = EmeraldGreen,
                    onClick = {
                        expanded = false
                        onNewSale()
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            containerColor = if (expanded) MaterialTheme.colorScheme.error else EmeraldGreen,
            contentColor = Color.White,
            modifier = Modifier.testTag("fab_quick_actions")
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Quick Actions"
            )
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 2.dp,
            modifier = Modifier.clickable { onClick() }
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = tint,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp))
        }
    }
}
