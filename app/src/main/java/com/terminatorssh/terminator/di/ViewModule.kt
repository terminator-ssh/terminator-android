package com.terminatorssh.terminator.di

import com.terminatorssh.terminator.ui.hosts.HostsViewModel
import com.terminatorssh.terminator.ui.login.LoginViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { HostsViewModel(get()) }
}