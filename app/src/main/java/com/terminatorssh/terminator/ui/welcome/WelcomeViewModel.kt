package com.terminatorssh.terminator.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.data.usecase.UnlockVaultUseCase
import com.terminatorssh.terminator.domain.repository.SessionRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val sessionRepository: SessionRepository,
    private val syncRepository: SyncRepository,
    private val unlockVaultUseCase: UnlockVaultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<WelcomeState>(WelcomeState.Loading)
    val state = _state.asStateFlow()

    private val _unlockError = MutableStateFlow<String?>(null)
    val unlockError = _unlockError.asStateFlow()

    init {
        checkUser()
    }

    private fun checkUser() {
        viewModelScope.launch {
            if (sessionRepository.hasUser()) {
                _state.value = WelcomeState.HasUser
            } else {
                _state.value = WelcomeState.NoUser
            }
        }
    }

    fun unlock(password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _unlockError.value = null

            val result = unlockVaultUseCase(password)

            result.onSuccess {
                launch { syncRepository.sync() }
                onSuccess()
            }.onFailure {
                _unlockError.value = "Incorrect Password"
            }
        }
    }

    fun wipeData() {
        viewModelScope.launch {
            sessionRepository.logout()
            _state.value = WelcomeState.NoUser
        }
    }
}