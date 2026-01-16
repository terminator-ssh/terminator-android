package com.terminatorssh.terminator.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.data.usecase.CreateLocalUserUseCase
import com.terminatorssh.terminator.data.usecase.LoginUseCase
import com.terminatorssh.terminator.data.usecase.RegisterUseCase
import com.terminatorssh.terminator.data.usecase.SmartConnectUseCase
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SetupViewModel(
    private val smartConnectUseCase: SmartConnectUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val createLocalUserUseCase: CreateLocalUserUseCase,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SetupState>(SetupState.Idle)
    val state: StateFlow<SetupState> = _state.asStateFlow()

    fun connect(url: String, username: String, pass: String) {
        if (url.isBlank() || username.isBlank() || pass.isBlank()) {
            _state.value = SetupState.Error("Please fill all fields")
            return
        }

        _state.value = SetupState.Loading

        viewModelScope.launch {
            val result = smartConnectUseCase(url, username, pass)

            result.onSuccess {
                syncRepository.sync()
                _state.value = SetupState.Success
            }.onFailure { error ->
                _state.value = SetupState.Error("Connection failed: ${error.message}")
            }
        }
    }

    fun createLocal(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _state.value = SetupState.Error("Please fill all fields")
            return
        }

        _state.value = SetupState.Loading

        viewModelScope.launch {
            val result = createLocalUserUseCase(username, pass)

            result.onSuccess {
                _state.value = SetupState.Success
            }.onFailure { error ->
                _state.value = SetupState.Error("User creation failed: ${error.message}")
            }
        }
    }

    fun resetState() {
        _state.value = SetupState.Idle
    }
}