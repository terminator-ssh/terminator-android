package com.terminatorssh.terminator.di

import com.terminatorssh.terminator.data.mapper.HostDataMapper
import com.terminatorssh.terminator.data.mapper.SyncMapper
import com.terminatorssh.terminator.data.remote.service.JSchSshService
import com.terminatorssh.terminator.data.remote.service.UserPersistenceService
import com.terminatorssh.terminator.data.repository.HostRepositoryImpl
import com.terminatorssh.terminator.data.repository.SessionRepositoryImpl
import com.terminatorssh.terminator.data.repository.SyncRepositoryImpl
import com.terminatorssh.terminator.data.usecase.CreateLocalUserUseCase
import com.terminatorssh.terminator.data.usecase.LoginUseCase
import com.terminatorssh.terminator.data.usecase.RegisterUseCase
import com.terminatorssh.terminator.data.usecase.UnlockVaultUseCase
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.repository.SessionRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import com.terminatorssh.terminator.domain.service.ArgonCryptoService
import com.terminatorssh.terminator.domain.service.AuthCryptoHelper
import com.terminatorssh.terminator.domain.service.CryptoService
import com.terminatorssh.terminator.domain.service.SshService
import org.koin.dsl.module

val appModule = module {
    includes(
        networkModule,
        databaseModule,
        viewModelModule
    )

    single { HostDataMapper(get(), get()) }
    single { SyncMapper() }

    single<CryptoService> { ArgonCryptoService() }

    single<SessionRepository> {
        SessionRepositoryImpl(
            get(),
            get()
        )
    }

    single<HostRepository> {
        HostRepositoryImpl(
            get(),
            get(),
            get()
        )
    }

    single<SyncRepository> {
        SyncRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single<SshService> { JSchSshService() }

    single { AuthCryptoHelper(
        get()) }

    single { UserPersistenceService(
        get(),
        get(),
        get()) }

    factory { LoginUseCase(
        get(),
        get(),
        get(),
        get()) }

    factory { RegisterUseCase(
        get(),
        get(),
        get(),
        get())
    }

    factory { UnlockVaultUseCase(
        get(),
        get(),
        get(),
        get(),
        get())
    }

    factory { CreateLocalUserUseCase(
        get(),
        get(),
        get())
    }
}