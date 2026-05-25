package com.example.kassaku.ui.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.ui.model.NotificationAccent
import com.example.kassaku.ui.model.NotificationInboxItem
import com.example.kassaku.ui.theme.LocalIsDark
import com.example.kassaku.ui.theme.StitchAccentRed
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchTextPrimary
import com.example.kassaku.ui.theme.StitchTextSecondary
import com.example.kassaku.ui.theme.iOSGroupedBackgroundDark
import com.example.kassaku.ui.theme.iOSGroupedBackgroundLight

@Composable
fun NotificationHistoryCard(
    item: NotificationInboxItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDark.current
    val theme = notificationAccentTheme(item.accent, isDark)
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        theme.backgroundStart.copy(alpha = if (isDark) 0.6f else 0.8f), 
                        theme.backgroundEnd.copy(alpha = if (isDark) 0.8f else 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.08f else 0.4f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(theme.iconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = mapNotificationIcon(item.iconKey),
                            contentDescription = null,
                            tint = theme.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.excerpt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = item.sentAtHuman,
                    style = MaterialTheme.typography.labelMedium,
                    color = textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(theme.chipBackground)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = item.category.replace("_", " "),
                        color = theme.chipText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!item.isRead) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(theme.iconTint)
                        )
                        Text(
                            text = "Baru",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.iconTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private data class NotificationCardTheme(
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val iconBackground: Color,
    val iconTint: Color,
    val chipBackground: Color,
    val chipText: Color
)

private fun notificationAccentTheme(accent: NotificationAccent, isDark: Boolean): NotificationCardTheme {
    val baseSurface = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight

    return when (accent) {
        NotificationAccent.EMERALD -> NotificationCardTheme(
            backgroundStart = if (isDark) baseSurface else Color(0xFFF8FFFC),
            backgroundEnd = if (isDark) Color(0xFF13221D) else Color(0xFFEDFDF5),
            iconBackground = StitchPrimary.copy(alpha = if (isDark) 0.2f else 0.12f),
            iconTint = StitchPrimary,
            chipBackground = StitchPrimary.copy(alpha = if (isDark) 0.2f else 0.12f),
            chipText = StitchPrimary
        )
        NotificationAccent.AMBER -> NotificationCardTheme(
            backgroundStart = if (isDark) baseSurface else Color(0xFFFFFCF3),
            backgroundEnd = if (isDark) Color(0xFF2A2211) else Color(0xFFFFF7D6),
            iconBackground = Color(0xFFF59E0B).copy(alpha = if (isDark) 0.22f else 0.14f),
            iconTint = Color(0xFFF59E0B),
            chipBackground = Color(0xFFF59E0B).copy(alpha = if (isDark) 0.22f else 0.14f),
            chipText = Color(0xFFF59E0B)
        )
        NotificationAccent.ROSE -> NotificationCardTheme(
            backgroundStart = if (isDark) baseSurface else Color(0xFFFFFAFA),
            backgroundEnd = if (isDark) Color(0xFF2A1717) else Color(0xFFFFE8E8),
            iconBackground = StitchAccentRed.copy(alpha = if (isDark) 0.22f else 0.12f),
            iconTint = StitchAccentRed,
            chipBackground = StitchAccentRed.copy(alpha = if (isDark) 0.22f else 0.12f),
            chipText = StitchAccentRed
        )
        NotificationAccent.SKY -> NotificationCardTheme(
            backgroundStart = if (isDark) baseSurface else Color(0xFFF8FBFF),
            backgroundEnd = if (isDark) Color(0xFF142231) else Color(0xFFEAF4FF),
            iconBackground = Color(0xFF3B82F6).copy(alpha = if (isDark) 0.22f else 0.12f),
            iconTint = Color(0xFF3B82F6),
            chipBackground = Color(0xFF3B82F6).copy(alpha = if (isDark) 0.22f else 0.12f),
            chipText = Color(0xFF3B82F6)
        )
        NotificationAccent.VIOLET -> NotificationCardTheme(
            backgroundStart = if (isDark) baseSurface else Color(0xFFFCFAFF),
            backgroundEnd = if (isDark) Color(0xFF20162D) else Color(0xFFF3E8FF),
            iconBackground = Color(0xFF8B5CF6).copy(alpha = if (isDark) 0.22f else 0.12f),
            iconTint = Color(0xFF8B5CF6),
            chipBackground = Color(0xFF8B5CF6).copy(alpha = if (isDark) 0.22f else 0.12f),
            chipText = Color(0xFF8B5CF6)
        )
        NotificationAccent.SLATE -> NotificationCardTheme(
            backgroundStart = baseSurface,
            backgroundEnd = if (isDark) Color(0xFF232833) else Color(0xFFF8FAFC),
            iconBackground = Color(0xFF64748B).copy(alpha = if (isDark) 0.22f else 0.1f),
            iconTint = Color(0xFF64748B),
            chipBackground = Color(0xFF64748B).copy(alpha = if (isDark) 0.22f else 0.1f),
            chipText = Color(0xFF64748B)
        )
    }
}

private fun mapNotificationIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "payments" -> Icons.Rounded.Payments
        "campaign" -> Icons.Rounded.Campaign
        "warning" -> Icons.Rounded.Warning
        "edit_note" -> Icons.Rounded.EditNote
        "auto_awesome" -> Icons.Rounded.AutoAwesome
        else -> Icons.Rounded.Notifications
    }
}
