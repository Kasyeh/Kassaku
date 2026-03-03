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


fun formatCurrencyFlexible(amount: Number): String {
    val absAmount = abs(amount.toDouble())
    return when {
        absAmount >= 1_000_000_000 -> {
            val formatted = String.format("%.1f", amount.toDouble() / 1_000_000_000).replace(".0", "")
            "Rp $formatted milliar"
        }
        absAmount >= 100_000_000 -> {
            val formatted = String.format("%.1f", amount.toDouble() / 1_000_000).replace(".0", "")
            "Rp $formatted jt"
        }
        else -> {
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.maximumFractionDigits = 0
            format.format(amount)
        }
    }
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
