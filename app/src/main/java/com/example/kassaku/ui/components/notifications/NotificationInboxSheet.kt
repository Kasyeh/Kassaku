package com.example.kassaku.ui.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kassaku.ui.model.NotificationInboxItem
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.LocalIsDark
import com.example.kassaku.ui.theme.StitchPrimary
import com.example.kassaku.ui.theme.StitchTextPrimary
import com.example.kassaku.ui.theme.StitchTextSecondary
import com.example.kassaku.ui.theme.iOSGroupedBackgroundDark
import com.example.kassaku.ui.theme.iOSGroupedBackgroundLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInboxSheet(
    isVisible: Boolean,
    notificationState: com.example.kassaku.viewmodel.NotificationUiState,
    unreadCount: Int,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onMarkAllRead: () -> Unit,
    onItemClick: (NotificationInboxItem) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible) return

    val isDark = LocalIsDark.current
    val textPrimary = if (isDark) Color.White else StitchTextPrimary
    val textSecondary = if (isDark) Color(0xFF94A3B8) else StitchTextSecondary
    val sheetColor = if (isDark) Color(0xFF0E1017) else Color(0xFFF8FAFC)
    val cardColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Notifikasi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Reminder, transaksi, dan pesan admin",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF151823) else Color(0xFF1E293B))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Belum dibaca",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.68f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = unreadCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(StitchPrimary.copy(alpha = if (isDark) 0.18f else 0.08f))
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            tint = StitchPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sinkron aktif",
                            style = MaterialTheme.typography.labelMedium,
                            color = StitchPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Daftar notifikasi disinkronkan lintas device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = textSecondary,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onMarkAllRead,
                enabled = unreadCount > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
            ) {
                Text(
                    text = "Tandai dibaca",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (notificationState) {
                is com.example.kassaku.viewmodel.NotificationUiState.Idle,
                is com.example.kassaku.viewmodel.NotificationUiState.Loading -> {
                    NotificationInboxSkeleton()
                }
                is com.example.kassaku.viewmodel.NotificationUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardColor)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = notificationState.message,
                            color = textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(onClick = onRetry) {
                            Text("Coba lagi")
                        }
                    }
                }
                is com.example.kassaku.viewmodel.NotificationUiState.Success -> {
                    if (notificationState.items.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(cardColor)
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(StitchPrimary.copy(alpha = if (isDark) 0.18f else 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Rounded.NotificationsNone,
                                    contentDescription = null,
                                    tint = StitchPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Belum ada notifikasi",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Setelah reminder atau alert baru terkirim, daftar notifikasinya akan muncul di sini.",
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(notificationState.items, key = { it.id }) { item ->
                                NotificationHistoryCard(
                                    item = item,
                                    onClick = { onItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
