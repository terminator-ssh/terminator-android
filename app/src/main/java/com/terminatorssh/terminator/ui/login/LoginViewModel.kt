package com.terminatorssh.terminator.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
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
            val loginResult = authRepository.login(url, username, pass)

            loginResult.onFailure { error ->
                _state.value = LoginState.Error(error.message ?: "Unknown login error")
            }

            val syncResult = syncRepository.sync()

            syncResult.onSuccess {
                _state.value = LoginState.Success
            }.onFailure { error ->
                _state.value = LoginState.Error("Sync failed: ${error.message}")
            }
        }
    }

    fun register(url: String, username: String, pass: String) {
        if (url.isBlank() || username.isBlank() || pass.isBlank()) {
            _state.value = LoginState.Error("Please fill all fields")
            return
        }

        _state.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.registerAndSync(url, username, pass)

            result.onSuccess {
                syncRepository.sync() // verify connection
                _state.value = LoginState.Success
            }.onFailure { error ->
                _state.value = LoginState.Error("Registration failed: ${error.message}")
            }
        }
    }

    fun createLocal(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _state.value = LoginState.Error("Please fill all fields")
            return
        }

        _state.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.createLocalUser(username, pass)

            result.onSuccess {
                _state.value = LoginState.Success
            }.onFailure { error ->
                _state.value = LoginState.Error("User creation failed: ${error.message}")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}