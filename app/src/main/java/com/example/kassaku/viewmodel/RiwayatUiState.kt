package com.example.kassaku.viewmodel

import com.example.kassaku.data.remote.model.RiwayatItem

/**
 * Represents the different states for the Riwayat (History) screen.
 */
sealed interface RiwayatUiState {
    /**
     * The initial state, or when no data loading operation is in progress.
     */
    object Idle : RiwayatUiState

    /**
     * Indicates that riwayat data is currently being loaded.
     */
    object Loading : RiwayatUiState

    /**
     * Indicates that riwayat data has been successfully loaded.
     * @param riwayatItems The list of history items.
     */
    data class Success(val riwayatItems: List<RiwayatItem>) : RiwayatUiState

    /**
     * Indicates that an error occurred while trying to load riwayat data.
     * @param message A descriptive error message.
     */
    data class Error(val message: String) : RiwayatUiState
}
