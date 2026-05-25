package com.example.kassaku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kassaku.data.remote.ApiClient
import com.example.kassaku.data.remote.model.ChatbotMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatbotViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatbotMessage>>(emptyList())
    val messages: StateFlow<List<ChatbotMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Welcome message
        _messages.value = listOf(
            ChatbotMessage(
                type = "bot",
                text = "Halo! Aku Asisten Cerdas KasSaku. Ada yang bisa kubantu terkait pengeluaran, pemasukan, atau saldo kamu hari ini?"
            )
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Tambahkan pesan user ke daftar
        val userMsg = ChatbotMessage(type = "user", text = text)
        _messages.value = _messages.value + userMsg

        // 2. Tampilkan loading
        _isLoading.value = true

        // 3. Hit API
        viewModelScope.launch {
            try {
                val response = ApiClient.api.askChatbot(text)
                if (response.isSuccessful && response.body() != null) {
                    val botMsg = response.body()!!.data
                    _messages.value = _messages.value + botMsg
                } else {
                    _messages.value = _messages.value + ChatbotMessage(
                        type = "bot",
                        text = "Maaf, aku sedang tidak bisa merespons saat ini. (Error ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatbotMessage(
                    type = "bot",
                    text = "Aduh, sepertinya ada masalah koneksi. Coba lagi nanti ya."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetChat(onSuccess: () -> Unit = {}) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = ApiClient.api.resetChatbot()
                if (response.isSuccessful) {
                    _messages.value = listOf(
                        ChatbotMessage(
                            type = "bot",
                            text = "Riwayat chat kita sudah dibersihkan ya! Ada yang bisa aku bantu dari awal? 😊"
                        )
                    )
                    onSuccess()
                } else {
                    _messages.value = _messages.value + ChatbotMessage(
                        type = "bot",
                        text = "Gagal mereset chat di server (Error ${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatbotMessage(
                    type = "bot",
                    text = "Gagal terhubung ke server untuk mereset chat."
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
