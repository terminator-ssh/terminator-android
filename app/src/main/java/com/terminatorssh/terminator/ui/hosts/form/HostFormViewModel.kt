package com.terminatorssh.terminator.ui.hosts.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.terminatorssh.terminator.domain.model.Host
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HostFormViewModel(
    private val hostRepository: HostRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    val name = MutableStateFlow("")
    val hostname = MutableStateFlow("")
    val port = MutableStateFlow("22")
    val username = MutableStateFlow("root")
    val password = MutableStateFlow("")

    private var currentHostId: String? = null

    fun loadHost(hostId: String?) {
        if (hostId == null) return

        currentHostId = hostId
        viewModelScope.launch {
            _isLoading.value = true
            val host = hostRepository.findHost(hostId)
            if (host != null) {
                name.value = host.name
                hostname.value = host.host
                port.value = host.port.toString()
                username.value = host.username
                password.value = host.password ?: ""
            }
            _isLoading.value = false
        }
    }

    fun save() {
        viewModelScope.launch {
            val portInt = port.value.toIntOrNull() ?: 22

            val idToSave = currentHostId ?: UUID.randomUUID().toString()

            val host = Host(
                id = idToSave,
                name = name.value.ifBlank { hostname.value },
                host = hostname.value,
                port = portInt,
                username = username.value,
                password = password.value.ifBlank { null }
            )

            hostRepository.saveHost(host)

            launch { syncRepository.sync() }

            _isSaved.value = true
        }
    }

    fun delete() {
        val id = currentHostId ?: return
        viewModelScope.launch {
            hostRepository.deleteHost(id)
            launch { syncRepository.sync() }
            _isSaved.value = true
        }
    }
}