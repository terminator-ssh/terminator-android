package com.terminatorssh.terminator.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.service.SshService
import jackpal.androidterm.emulatorview.TermSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TerminalState {
    data object Connecting : TerminalState()
    data class Connected(val session: TermSession) : TerminalState()
    data class Error(val message: String) : TerminalState()
    data object Disconnected : TerminalState()
}

class TerminalViewModel(
    private val hostRepository: HostRepository,
    private val sshService: SshService
) : ViewModel() {

    private val _state = MutableStateFlow<TerminalState>(TerminalState.Connecting)
    val state = _state.asStateFlow()

    fun connect(hostId: String) {
        viewModelScope.launch {
            try {
                val host = hostRepository.findHost(hostId)
                    ?: throw Exception("Host not found")

                val connection = sshService.connect(
                    host = host.host,
                    port = host.port,
                    username = host.username,
                    password = host.password,
                    privateKey = host.privateKey
                )

                val termSession = SshTerminalSession(
                    connection.inputStream,
                    connection.outputStream
                )

                _state.value = TerminalState.Connected(termSession)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = TerminalState.Error(e.message ?: "Connection Failed")
            }
        }
    }

    fun disconnect() {
        sshService.disconnect()
        _state.value = TerminalState.Disconnected
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}