package com.example.taller_android.ui

import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la autenticación y estado del usuario en Firebase.
 *
 * @author Santiago
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _user = mutableStateOf(auth.currentUser)
    val user: State<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Formato de correo no válido"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña es requerida"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }

    private fun getSpanishErrorMessage(throwable: Throwable): String {
        val message = throwable.localizedMessage ?: throwable.message ?: ""
        return when {
            message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                "Error de configuración: Desactiva la 'Protección de enumeración de correo' o reCAPTCHA en la consola de Firebase."
            message.contains("already in use", ignoreCase = true) || message.contains("email-already-in-use", ignoreCase = true) ->
                "Este correo electrónico ya está registrado."
            message.contains("invalid credential", ignoreCase = true) || message.contains("invalid-credential", ignoreCase = true) || message.contains("wrong-password", ignoreCase = true) ->
                "Correo o contraseña incorrectos."
            message.contains("user-not-found", ignoreCase = true) || message.contains("no user record", ignoreCase = true) ->
                "No existe una cuenta registrada con este correo."
            message.contains("weak-password", ignoreCase = true) ->
                "La contraseña es demasiado débil (mínimo 6 caracteres)."
            message.contains("network", ignoreCase = true) || message.contains("network-request-failed", ignoreCase = true) ->
                "Error de conexión. Verifica tu internet."
            else -> "Error: $message"
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val emailErr = validateEmail(email)
        val passErr = validatePassword(pass)
        if (emailErr != null || passErr != null) {
            onError(emailErr ?: passErr ?: "Datos inválidos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            runCatching { auth.signInWithEmailAndPassword(email.trim(), pass).await() }
                .onSuccess {
                    _user.value = auth.currentUser
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    onError(getSpanishErrorMessage(it))
                }
        }
    }

    fun register(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val emailErr = validateEmail(email)
        val passErr = validatePassword(pass)
        if (emailErr != null || passErr != null) {
            onError(emailErr ?: passErr ?: "Datos inválidos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userData = hashMapOf(
                        "uid" to firebaseUser.uid,
                        "email" to email.trim(),
                        "createdAt" to Timestamp.now()
                    )
                    firestore.collection("users").document(firebaseUser.uid).set(userData).await()
                }
            }
                .onSuccess {
                    _user.value = auth.currentUser
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    onError(getSpanishErrorMessage(it))
                }
        }
    }

    fun logout() {
        auth.signOut()
        _user.value = null
    }
}
