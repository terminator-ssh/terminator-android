package com.terminatorssh.terminator.domain.service

import com.terminatorssh.terminator.domain.model.SshConnection

interface SshService {
    suspend fun connect(
        host: String,
        port: Int,
        username: String,
        password: String?,
        privateKey: String?
    ): SshConnection

    fun disconnect()
}