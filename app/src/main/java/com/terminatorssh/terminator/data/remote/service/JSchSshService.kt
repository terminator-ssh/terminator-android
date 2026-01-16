package com.terminatorssh.terminator.data.remote.service

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.terminatorssh.terminator.data.common.SshConstants
import com.terminatorssh.terminator.domain.model.SshConnection
import com.terminatorssh.terminator.domain.service.SshService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

class JSchSshService : SshService {

    private var activeSession: Session? = null
    private var activeChannel: com.jcraft.jsch.ChannelShell? = null

    override suspend fun connect(
        host: String,
        port: Int,
        username: String,
        password: String?,
        privateKey: String?
    ): SshConnection = withContext(Dispatchers.IO) {
        val jsch = JSch()

        if (!privateKey.isNullOrBlank()) {
            val keyBytes = privateKey.toByteArray(Charsets.UTF_8)
            jsch.addIdentity(
                "identity", keyBytes, null, null)
        }

        val session = jsch.getSession(username, host, port)
        activeSession = session

        if (!password.isNullOrBlank()) {
            session.setPassword(password)
        }

        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        session.setConfig(config)

        session.connect(SshConstants.SSH_CONNECTION_TIMEOUT_MS)

        val channel = session.openChannel("shell") as com.jcraft.jsch.ChannelShell
        channel.setPty(true)
        channel.setPtyType("xterm")

        val inputStream = channel.inputStream
        val outputStream = channel.outputStream

        channel.connect()
        activeChannel = channel

        return@withContext SshConnection(inputStream, outputStream, session)
    }

    override fun disconnect() {
        try {
            activeChannel?.disconnect()
            activeSession?.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}