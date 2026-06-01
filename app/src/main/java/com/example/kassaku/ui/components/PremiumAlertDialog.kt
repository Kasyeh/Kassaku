package com.example.kassaku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kassaku.ui.theme.*

@Composable
fun PremiumAlertDialog(
    title: String,
    text: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = StitchPrimary,
    confirmText: String = "Ya",
    confirmColor: Color = StitchPrimary,
    confirmTextColor: Color = StitchTextPrimary,
    confirmEnabled: Boolean = true,
    isConfirmLoading: Boolean = false,
    showDismissButton: Boolean = true,
    dismissText: String = "Batal",
    dismissTextColor: Color = StitchAccentRed,
    isDark: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    val surfaceColor = if (isDark) StitchSurfaceDark else StitchSurfaceLight
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (text != null) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                content?.invoke()
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isConfirmLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = confirmTextColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = confirmText, color = confirmTextColor, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = if (showDismissButton) {
            {
                TextButton(onClick = onDismissRequest) {
                    Text(text = dismissText, color = dismissTextColor, fontWeight = FontWeight.Bold)
                }
            }
        } else null,
        shape = RoundedCornerShape(28.dp),
        containerColor = surfaceColor
    )
}
