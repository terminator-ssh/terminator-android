package com.terminatorssh.terminator.ui.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.domain.repository.HostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HostsViewModel(
    private val hostRepository: HostRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HostsState>(HostsState.Loading)
    val state = _state.asStateFlow()

    init {
        loadHosts()
    }

    fun loadHosts() {
        viewModelScope.launch {
            _state.value = HostsState.Loading
            val list = hostRepository.getHosts()
            if (list.isEmpty()) {
                _state.value = HostsState.Empty
            } else {
                _state.value = HostsState.Success(list)
            }
        }
    }
}