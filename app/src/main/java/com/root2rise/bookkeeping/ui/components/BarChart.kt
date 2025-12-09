package com.root2rise.bookkeeping.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.root2rise.bookkeeping.ui.theme.DarkCard
import com.root2rise.bookkeeping.ui.theme.TextSecondary
import com.root2rise.bookkeeping.ui.theme.TextTertiary
import java.util.Locale

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to display",
                color = TextTertiary,
                fontSize = 14.sp
            )
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val chartHeight = 200.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkCard.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = (canvasWidth / data.size) * 0.6f
                val spacing = (canvasWidth / data.size) * 0.4f

                data.forEachIndexed { index, item ->
                    val barHeight = (item.value / maxValue) * canvasHeight
                    val x = (index * (barWidth + spacing)) + (spacing / 2)
                    val y = canvasHeight - barHeight

                    // Draw bar with gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                item.color,
                                item.color.copy(alpha = 0.6f)
                            )
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = formatValue(item.value),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = item.color
                    )
                }
            }
        }
    }
}

private fun formatValue(value: Float): String {
    return when {
        value >= 100000 -> "₹${String.format(Locale.getDefault(), "%.1f", value / 100000)}L"
        value >= 1000 -> "₹${String.format(Locale.getDefault(), "%.1f", value / 1000)}K"
        else -> "₹${value.toInt()}"
    }
}
