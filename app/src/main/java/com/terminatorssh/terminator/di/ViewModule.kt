package com.terminatorssh.terminator.di

import com.terminatorssh.terminator.ui.hosts.HostsViewModel
import com.terminatorssh.terminator.ui.hosts.form.HostFormViewModel
import com.terminatorssh.terminator.ui.setup.SetupViewModel
import com.terminatorssh.terminator.ui.terminal.TerminalViewModel
import com.terminatorssh.terminator.ui.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SetupViewModel(
        get(),
        get(),
        get(),
        get()) }

    viewModel { HostsViewModel(
        get(),
        get()) }

    viewModel { HostFormViewModel(
        get(),
        get()) }

    viewModel { TerminalViewModel(
        get(),
        get()) }

    viewModel { WelcomeViewModel(
        get(),
        get(),
        get()) }
}