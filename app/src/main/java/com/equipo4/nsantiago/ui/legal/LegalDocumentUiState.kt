package com.equipo4.nsantiago.ui.legal

data class LegalDocumentUiState(
    val isLoading: Boolean = false,
    val documentData: LegalDocumentData? = null,
    val activeSectionIndex: Int = 0,
    val isDropdownExpanded: Boolean = false,
    val errorMessage: String? = null
)