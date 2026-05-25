package com.example.kassaku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.data.remote.model.NudgeItem
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*

@Composable
fun SmartNudgeCard(
    nudge: NudgeItem,
    isDark: Boolean,
    onActionClick: (String) -> Unit
) {
    val icon = when (nudge.type) {
        "success" -> Icons.Rounded.CheckCircle
        "warning" -> Icons.Rounded.Warning
        else -> Icons.Rounded.Info
    }
    
    val iconTint = when (nudge.type) {
        "success" -> Color(0xFF10B981)
        "warning" -> Color(0xFFF59E0B)
        else -> StitchPrimary
    }
    
    val bgColor = when (nudge.type) {
        "success" -> if (isDark) Color(0xFF064E3B) else Color(0xFFD1FAE5)
        "warning" -> if (isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)
        else -> if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
    }

    val cardBg = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val textColor = if (isDark) iOSLabelDark else iOSLabelLight
    val secondaryTextColor = if (isDark) iOSSecondaryLabelDark else iOSSecondaryLabelLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = iconTint.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(KassakuSpacing.cardInner)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(KassakuSpacing.elementGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nudge.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nudge.message,
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onActionClick(nudge.actionType) },
                    colors = ButtonDefaults.buttonColors(containerColor = iconTint),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = nudge.actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
