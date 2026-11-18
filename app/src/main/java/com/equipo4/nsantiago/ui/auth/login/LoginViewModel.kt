package com.equipo4.nsantiago.ui.auth.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nsantiago.data.local.SessionManager
import com.equipo4.nsantiago.di.NetworkModule
import com.equipo4.nsantiago.network.AuthApi
import com.equipo4.nsantiago.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import android.content.Context
import com.equipo4.nsantiago.data.remote.GoogleAuthManager
import com.equipo4.nsantiago.data.repository.AuthRepository

import com.equipo4.nsantiago.util.AppStrings
import com.equipo4.nsantiago.R

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)
    private val authRepository = AuthRepository(authApi)

    private val _uiState = MutableStateFlow(
        LoginUiState(
            errorMessage = if (sessionManager.consumeSessionExpired())
                AppStrings.get(R.string.login_session_expired)
            else null
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        _uiState.update {
            it.copy(
                email = email,
                isEmailValid = isValid,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        val isValid = password.isNotEmpty()
        _uiState.update {
            it.copy(
                password = password,
                isPasswordValid = isValid,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        if (!currentState.isFormValid || currentState.isLoading) return
        val correo = currentState.email.trim().ifBlank { "usuario@nsantiago.demo" }
        sessionManager.saveSession(
            token = "sesion-simulada",
            userId = 1,
            nombre = "Usuario N'Santiago",
            rol = "VENDEDOR",
            correo = correo
        )
        _uiState.update { it.copy(isLoading = false, isSuccess = true, errorMessage = null) }
    }

    /**
     * Prioridad: 1) el mensaje que manda la API; 2) un texto según el código HTTP.
     * Así "Demasiados intentos" y "Cuenta suspendida" ya no se convierten en
     * AppStrings.get(R.string.login_error_401).
     */
    private fun parseErrorMessage(code: Int, rawError: String?): String {
        val apiMessage = try {
            val json = JSONObject(rawError ?: "")
            listOf("error", "message", "mensaje", "detail").firstNotNullOfOrNull { key ->
                json.optString(key, "").takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            null
        }
        if (apiMessage != null) return apiMessage

        return when (code) {
            401 -> AppStrings.get(R.string.login_error_401)
            403 -> AppStrings.get(R.string.login_error_403)
            429 -> AppStrings.get(R.string.login_error_429)
            else -> AppStrings.get(R.string.error_unexpected_retry)
        }
    }

    fun onGoogleLoginClicked(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    googleSuccessMessage = null
                )
            }

            val googleAuthManager = GoogleAuthManager(context)
            val result = googleAuthManager.getFirebaseIdToken()

            result.fold(
                onSuccess = { firebaseToken ->
                    if (firebaseToken == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        return@fold
                    }
                    viewModelScope.launch {
                        val result = authRepository.loginWithGoogle(firebaseToken)
                        result.fold(
                            onSuccess = { googleResult ->
                                if (googleResult.status == "SUCCESS" &&
                                    !googleResult.sessionToken.isNullOrBlank()
                                ) {
                                    sessionManager.saveSession(
                                        token = googleResult.sessionToken,
                                        userId = googleResult.userId?.toIntOrNull() ?: 0,
                                        nombre = googleResult.email.orEmpty(),
                                        rol = "VENDEDOR",
                                        correo = googleResult.email.orEmpty()
                                    )
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            isSuccess = true,
                                            googleSuccessMessage = "Inicio con Google completado"
                                        )
                                    }
                                } else {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            errorMessage = "Tu cuenta de Google necesita completar el registro en la aplicación."
                                        )
                                    }
                                }
                            },
                            onFailure = { error ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = error.message
                                            ?: "No se pudo iniciar sesión con Google."
                                    )
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo iniciar sesión con Google. Inténtalo de nuevo."
                        )
                    }
                }
            )
        }
    }
}