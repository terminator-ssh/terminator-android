package com.terminatorssh.terminator.ui.hosts.form

sealed class HostFormAction {
    data object New : HostFormAction()
    data class Edit(val hostId: String) : HostFormAction()
}