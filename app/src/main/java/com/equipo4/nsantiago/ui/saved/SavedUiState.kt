package com.equipo4.nsantiago.ui.saved

import com.equipo4.nsantiago.domain.model.Property

data class SavedUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val properties: List<Property> = emptyList()
)
