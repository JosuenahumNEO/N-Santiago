package com.equipo4.nsantiago.ui.auth.login

data class LoginUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val password: String = "",
    val isPasswordValid: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val googleSuccessMessage: String? = null
) {
    // Modo demostración: el acceso no requiere validar credenciales reales.
    val isFormValid: Boolean
        get() = !isLoading
}