package com.terminatorssh.terminator.ui.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HostsViewModel(
    private val hostRepository: HostRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val state: StateFlow<HostsState> = hostRepository.getHostsFlow()
        .map { list ->
            if (list.isEmpty())
                HostsState.Empty
            else
                HostsState.Success(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HostsState.Loading
        )

    fun refresh() {
        viewModelScope.launch {
            syncRepository.sync()
        }
    }
}