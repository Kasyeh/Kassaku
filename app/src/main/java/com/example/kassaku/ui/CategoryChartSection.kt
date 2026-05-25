package com.example.kassaku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.data.remote.model.StatistikData

@Composable
fun CategoryChartSection(
    data: StatistikData,
    selectedPeriod: String,
    isDark: Boolean
) {
    var isExpense by remember { mutableStateOf(true) }
    
    // Asumsikan resolveCashflowPeriodData dipanggil di StatistikScreen.kt, atau kita buat helper lokal:
    val periodData = data.cashflowSeries?.get(selectedPeriod) ?: data.cashflowSeries?.values?.firstOrNull()
    
    val categoryData = if (isExpense) periodData?.categoriesExpense ?: emptyMap() else periodData?.categoriesIncome ?: emptyMap()
    val totalAmount = if (isExpense) periodData?.totalExpense ?: 0.0 else periodData?.totalIncome ?: 0.0
    
    val sortedData = categoryData.entries.sortedByDescending { it.value }
    
    // Generate colors
    val colors = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFF14B8A6), // Teal
        Color(0xFFF97316), // Orange
        Color(0xFF6366F1), // Indigo
        Color(0xFF84CC16)  // Lime
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E293B),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isExpense) Color.Transparent else if (isDark) Color(0xFF334155) else Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isExpense = false }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Masuk",
                        color = if (!isExpense) (if (isDark) Color.White else Color(0xFF1E293B)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                        fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isExpense) Color.Transparent else if (isDark) Color(0xFF334155) else Color.White,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isExpense = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keluar",
                        color = if (isExpense) (if (isDark) Color.White else Color(0xFF1E293B)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                        fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (sortedData.isEmpty() || totalAmount == 0.0) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada data",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            } else {
                // Doughnut Chart
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val strokeWidth = 30.dp.toPx()
                        
                        sortedData.forEachIndexed { index, entry ->
                            val sweepAngle = ((entry.value / totalAmount) * 360f).toFloat()
                            val color = colors[index % colors.size]
                            
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Butt
                                ),
                                size = size.copy(
                                    width = size.width - strokeWidth,
                                    height = size.height - strokeWidth
                                ),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center Text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isExpense) "Total Keluar" else "Total Masuk",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Text(
                            text = formatCurrency(totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Legend
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sortedData.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors[index % colors.size], CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = entry.key,
                                fontSize = 14.sp,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                modifier = Modifier.weight(1f)
                            )
                            val percentage = (entry.value / totalAmount) * 100
                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }
    }
}
