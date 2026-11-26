package br.edu.iftm.gestorpendencias.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.iftm.gestorpendencias.util.Validacao
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val email: String = "",
    val senha: String = "",
    val modoCadastro: Boolean = false,
    val carregando: Boolean = false,
    val erro: String? = null,
    val autenticado: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun onEmail(v: String) = _ui.update { it.copy(email = v, erro = null) }
    fun onSenha(v: String) = _ui.update { it.copy(senha = v, erro = null) }
    fun alternarModo() = _ui.update { it.copy(modoCadastro = !it.modoCadastro, erro = null) }

    fun enviar() {
        val s = _ui.value
        val erro = Validacao.email(s.email) ?: Validacao.senha(s.senha)
        if (erro != null) {
            _ui.update { it.copy(erro = erro) }
            return
        }
        _ui.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            try {
                val email = s.email.trim()
                if (s.modoCadastro) auth.createUserWithEmailAndPassword(email, s.senha).await()
                else auth.signInWithEmailAndPassword(email, s.senha).await()
                _ui.update { it.copy(carregando = false, autenticado = true) }
            } catch (ex: Exception) {
                _ui.update { it.copy(carregando = false, erro = traduzir(ex)) }
            }
        }
    }

    private fun traduzir(ex: Exception): String = when (ex) {
        is FirebaseAuthWeakPasswordException -> "Senha muito fraca"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos"
        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado"
        is FirebaseNetworkException -> "Sem conexão com a internet"
        else -> "Falha na autenticação: ${ex.localizedMessage}"
    }
}
