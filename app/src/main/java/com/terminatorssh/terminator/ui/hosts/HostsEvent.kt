package com.terminatorssh.terminator.ui.hosts

sealed class HostsEvent {
    data class ShowSnackbar(val message: String) : HostsEvent()
    data object NavigateToWelcome : HostsEvent()
}