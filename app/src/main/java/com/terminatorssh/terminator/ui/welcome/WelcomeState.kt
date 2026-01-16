package com.terminatorssh.terminator.ui.welcome

sealed class WelcomeState {
    data object Loading : WelcomeState()
    data object NoUser : WelcomeState()
    data object HasUser : WelcomeState()
}