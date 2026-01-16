package com.terminatorssh.terminator.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(url: String, username: String, pass: String) {
        if (url.isBlank() || username.isBlank() || pass.isBlank()) {
            _state.value = LoginState.Error("Please fill all fields")
            return
        }

        _state.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.loginAndSync(url, username, pass)

            result.onSuccess {
                _state.value = LoginState.Success
            }.onFailure { error ->
                _state.value = LoginState.Error(error.message ?: "Unknown login error")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}