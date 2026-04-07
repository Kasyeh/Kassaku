package com.example.kassaku.utils

import com.example.kassaku.viewmodel.LogoutReason
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ForceLogoutManager {
    private val _events = MutableSharedFlow<LogoutReason>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun trigger(reason: LogoutReason = LogoutReason.BLOCKED) {
        _events.tryEmit(reason)
    }
}
