package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyLight

data class ChartBarData(
    val label: String,
    val value: Long,
    val profit: Long = 0
)

@Composable
fun SalesBarChart(
    title: String,
    data: List<ChartBarData>,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NavyLight))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sales", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Profit", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxValue = (data.maxOfOrNull { it.value } ?: 100L).coerceAtLeast(1L)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val barGroupWidth = size.width / data.size.coerceAtLeast(1)
                    val barWidth = barGroupWidth * 0.35f
                    val chartHeight = size.height - 20.dp.toPx()

                    // Horizontal gridlines
                    for (i in 1..3) {
                        val y = chartHeight * (i / 4f)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    data.forEachIndexed { index, item ->
                        val groupX = index * barGroupWidth
                        val salesHeight = (item.value.toFloat() / maxValue) * chartHeight
                        val profitHeight = (item.profit.toFloat() / maxValue) * chartHeight

                        // Sales bar
                        drawRoundRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(groupX + (barGroupWidth * 0.1f), chartHeight - salesHeight),
                            size = Size(barWidth, salesHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )

                        // Profit bar
                        drawRoundRect(
                            color = EmeraldGreen,
                            topLeft = Offset(groupX + (barGroupWidth * 0.1f) + barWidth + 2.dp.toPx(), chartHeight - profitHeight),
                            size = Size(barWidth, profitHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }

            // X-axis labels
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { item ->
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CashFlowForecastChart(
    title: String,
    points: List<Long>,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Projected 30-day liquidity trajectory",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (points.isNotEmpty()) {
                val minVal = points.minOrNull() ?: 0L
                val maxVal = points.maxOrNull() ?: 100L
                val range = (maxVal - minVal).coerceAtLeast(1L)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                    val path = Path()

                    points.forEachIndexed { index, value ->
                        val x = index * stepX
                        val normalized = (value - minVal).toFloat() / range
                        val y = size.height - (normalized * (size.height - 16.dp.toPx())) - 8.dp.toPx()

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }

                        // Point circle
                        drawCircle(
                            color = EmeraldGreen,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Stroke curve
                    drawPath(
                        path = path,
                        color = EmeraldGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
