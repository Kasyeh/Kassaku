package com.example.kassaku.viewmodel

import com.example.kassaku.data.remote.model.ImpianItem

/**
 * Represents the different states for the Impian (Dreams) screen.
 */
sealed interface ImpianUiState {
    /**
     * The initial state, or when no data loading operation is in progress.
     */
    object Idle : ImpianUiState

    /**
     * Indicates that impian data is currently being loaded.
     */
    object Loading : ImpianUiState

    /**
     * Indicates that impian data has been successfully loaded.
     * @param impianItems The list of dream items.
     */
    data class Success(val impianItems: List<ImpianItem>) : ImpianUiState

    /**
     * Indicates that an error occurred while trying to load impian data.
     * @param message A descriptive error message.
     */
    data class Error(val message: String) : ImpianUiState
}
