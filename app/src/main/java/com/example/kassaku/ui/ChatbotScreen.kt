package com.example.kassaku.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kassaku.data.remote.model.ChatbotMessage
import com.example.kassaku.ui.theme.KassakuSpacing
import com.example.kassaku.ui.theme.*
import com.example.kassaku.viewmodel.ChatbotViewModel

@Composable
fun rememberMarkdownAnnotatedString(text: String?, boldColor: Color): AnnotatedString {
    val safeText = text.orEmpty()
    return remember(safeText, boldColor) {
        buildAnnotatedString {
            var currentIndex = 0
            val pattern = Regex("\\*\\*(.*?)\\*\\*")
            val matches = pattern.findAll(safeText)
            
            for (match in matches) {
                if (match.range.first > currentIndex) {
                    append(safeText.substring(currentIndex, match.range.first))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                    append(match.groupValues[1])
                }
                currentIndex = match.range.last + 1
            }
            
            if (currentIndex < safeText.length) {
                append(safeText.substring(currentIndex))
            }
        }
    }
}

@Composable
fun TypingIndicator(isDark: Boolean) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dotColor = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93)
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "typing_dot_$index")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 600
                        0.2f at (index * 150)
                        1.0f at (index * 150 + 150)
                        0.2f at (index * 150 + 300)
                    },
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor.copy(alpha = alpha))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onBackClick: () -> Unit,
    chatbotViewModel: ChatbotViewModel = viewModel()
) {
    val messages by chatbotViewModel.messages.collectAsStateWithLifecycle()
    val isLoading by chatbotViewModel.isLoading.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    val isDark = LocalIsDark.current

    val bgColor = if (isDark) iOSBackgroundDark else iOSBackgroundLight
    val surfaceColor = if (isDark) iOSGroupedBackgroundDark else iOSGroupedBackgroundLight
    val labelColor = if (isDark) iOSLabelDark else iOSLabelLight

    val listState = rememberLazyListState()

    // Smooth auto scroll to the bottom when messages size changes or loading state changes
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            val lastIndex = if (isLoading) messages.size else messages.size - 1
            listState.animateScrollToItem(lastIndex)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Hapus Riwayat Chat?", fontWeight = FontWeight.Bold) },
            text = { Text("Tindakan ini akan menghapus semua riwayat percakapan kita saat ini.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        chatbotViewModel.resetChat()
                    }
                ) {
                    Text("Ya, Bersihkan", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = surfaceColor,
            titleContentColor = labelColor,
            textContentColor = labelColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Asisten KasSaku", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = labelColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF34C759)) // iOS green color
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Online", fontSize = 11.sp, color = if (isDark) Color(0xFF8E8E93) else Color(0xFF8E8E93))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Kembali", tint = labelColor)
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Hapus Riwayat Chat", tint = labelColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = KassakuSpacing.screenHorizontal,
                    vertical = KassakuSpacing.screenVertical
                ),
                verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 4.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(message = msg, isDark = isDark)
                }
                
                if (messages.size <= 1 && !isLoading) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(KassakuSpacing.elementGap + 2.dp)
                        ) {
                            Text(
                                text = "REKOMENDASI PERTANYAAN:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF8E8E93) else Color(0xFFC7C7CC),
                                letterSpacing = 1.sp
                            )
                            
                            val suggestions = listOf(
                                "Berapa saldo saya?" to "Berapa saldo saya saat ini?",
                                "Pengeluaran bulan ini?" to "Berapa total pengeluaran saya bulan ini?",
                                "5 Transaksi terakhir?" to "Apa saja 5 transaksi terakhir saya?",
                                "Tips hemat menabung?" to "Berikan tips untuk menghemat pengeluaran!"
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(KassakuSpacing.chipRowGap),
                                contentPadding = PaddingValues(end = KassakuSpacing.screenHorizontal, bottom = 12.dp)
                            ) {
                                items(suggestions) { (label, question) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7))
                                            .border(
                                                width = 1.dp,
                                                color = if (isDark) Color(0xFF38383A) else Color(0xFFE5E5EA),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                chatbotViewModel.sendMessage(question)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = labelColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (isLoading) {
                    item {
                        ChatBubble(
                            message = ChatbotMessage("bot", ""),
                            isDark = isDark,
                            isLoading = true
                        )
                    }
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Tanyakan sesuatu...", color = if (isDark) Color(0xFF8E8E93) else Color(0xFFC7C7CC), fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                        unfocusedContainerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                        focusedTextColor = labelColor,
                        unfocusedTextColor = labelColor
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isLoading) {
                                chatbotViewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isLoading) {
                            chatbotViewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(StitchPrimary, RoundedCornerShape(23.dp)),
                    enabled = inputText.isNotBlank() && !isLoading
                ) {
                    Icon(Icons.Rounded.Send, contentDescription = "Kirim", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatbotMessage, 
    isDark: Boolean,
    isLoading: Boolean = false
) {
    val isUser = message.type == "user"
    val bubbleColor = if (isUser) {
        StitchPrimary
    } else {
        if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
    }
    
    val textColor = if (isUser) {
        Color.White
    } else {
        if (isDark) iOSLabelDark else iOSLabelLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // Small round robot avatar next to bot bubbles
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StitchPrimary.copy(alpha = 0.12f))
                    .border(
                        width = 1.dp,
                        color = StitchPrimary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("AI", fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (isLoading) {
                TypingIndicator(isDark = isDark)
            } else {
                val formattedText = rememberMarkdownAnnotatedString(text = message.text, boldColor = textColor)
                Text(
                    text = formattedText,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
