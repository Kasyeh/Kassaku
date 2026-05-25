package com.example.kassaku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.kassaku.ui.theme.StitchSurfaceDark
import com.example.kassaku.ui.theme.StitchTextPrimary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchSurfaceLight
import com.example.kassaku.ui.theme.StitchTextSecondary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun IconButtonSmall(
    icon: ImageVector,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isDark) StitchSurfaceDark else Color.White)
            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isDark) Color.White else StitchTextPrimary
        )
    }
}


fun formatCurrencyFlexible(
    amount: Number,
    currencyCode: String = "IDR",
    formatMode: String = "compact"
): String {
    if (formatMode.equals("standard", ignoreCase = true)) {
        return formatCurrencyExact(amount.toDouble(), currencyCode)
    }

    val absAmount = abs(amount.toDouble())
    val symbol = when (currencyCode.uppercase()) {
        "USD" -> "$"
        "MYR" -> "RM"
        "EUR" -> "€"
        "SGD" -> "S$"
        else -> "Rp"
    }

    return when {
        absAmount >= 1_000_000_000 -> {
            val formatted = String.format("%.1f", amount.toDouble() / 1_000_000_000).replace(".0", "")
            "$symbol $formatted miliar"
        }
        absAmount >= 100_000_000 -> {
            val formatted = String.format("%.1f", amount.toDouble() / 1_000_000).replace(".0", "")
            "$symbol $formatted jt"
        }
        else -> {
            formatCurrencyExact(amount.toDouble(), currencyCode)
        }
    }
}

fun formatCurrencyExact(amount: Double, currencyCode: String = "IDR"): String {
    val locale = when (currencyCode.uppercase()) {
        "USD" -> Locale("en", "US")
        "MYR" -> Locale("ms", "MY")
        "EUR" -> Locale("fr", "FR")
        "SGD" -> Locale("en", "SG")
        else -> Locale("in", "ID")
    }

    val format = NumberFormat.getCurrencyInstance(locale)
    if (currencyCode.uppercase() == "IDR") {
        format.maximumFractionDigits = 0
    } else {
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
    }
    
    var result = format.format(amount)
    
    // Replace default currency symbols with our preferred ones if needed
    result = result.replace("Rp", "Rp ")
                   .replace("US$", "$ ")
                   .replace("$", "$ ")
                   .replace("RM", "RM ")
                   .replace("SGD", "S$ ")
                   .replace("€", "€ ")
                   // Clean up double spaces
                   .replace("  ", " ")
                   .trim()
                   
    return result
}
@Composable
fun LogoutConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    isDark: Boolean
) {
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Default.Help,
                contentDescription = null,
                tint = StitchPrimary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Apakah yakin ingin logout?",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = textPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ya, Keluar!", color = StitchTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Batal", color = StitchAccentRed, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = surfaceColor
    )
}
