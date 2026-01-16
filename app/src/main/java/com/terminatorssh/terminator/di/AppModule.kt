package com.terminatorssh.terminator.di

import com.terminatorssh.terminator.data.remote.service.JSchSshService
import com.terminatorssh.terminator.data.repository.AuthRepositoryImpl
import com.terminatorssh.terminator.data.repository.HostRepositoryImpl
import com.terminatorssh.terminator.data.repository.SyncRepositoryImpl
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import com.terminatorssh.terminator.domain.service.ArgonCryptoService
import com.terminatorssh.terminator.domain.service.CryptoService
import com.terminatorssh.terminator.domain.service.SshService
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

    single<HostRepository> {
        HostRepositoryImpl(
            blobDao = get(),
            authRepository = get(),
            cryptoService = get(),
            gson = get()
        )
    }

    single<SyncRepository> {
        SyncRepositoryImpl(
            authRepository = get(),
            userDao = get(),
            blobDao = get(),
            clientFactory = get()
        )
    }

    single<SshService> { JSchSshService() }
}