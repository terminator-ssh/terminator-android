package com.terminatorssh.terminator.di

import com.terminatorssh.terminator.data.repository.AuthRepositoryImpl
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.service.ArgonCryptoService
import com.terminatorssh.terminator.domain.service.CryptoService
import org.koin.dsl.module

val appModule = module {
    includes(
        networkModule,
        databaseModule,
        viewModelModule
    )

    single<CryptoService> { ArgonCryptoService() }

    single<AuthRepository> {
        AuthRepositoryImpl(
            userDao = get(),
            clientFactory = get(),
            cryptoService = get()
        )
    }
}