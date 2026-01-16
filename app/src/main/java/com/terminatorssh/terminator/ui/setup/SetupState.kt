package com.terminatorssh.terminator.ui.setup

sealed class SetupState {
    data object Idle : SetupState()
    data object Loading : SetupState()
    data object Success : SetupState()
    data class Error(val message: String) : SetupState()
}