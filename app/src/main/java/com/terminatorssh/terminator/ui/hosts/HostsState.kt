package com.terminatorssh.terminator.ui.hosts

import com.terminatorssh.terminator.domain.model.Host

sealed class HostsState {
    data object Loading : HostsState()
    data class Success(val hosts: List<Host>) : HostsState()
    data object Empty : HostsState()
}