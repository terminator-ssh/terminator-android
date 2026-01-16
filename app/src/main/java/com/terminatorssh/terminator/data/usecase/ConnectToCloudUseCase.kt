package com.terminatorssh.terminator.data.usecase

import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.RegisterRequest
import com.terminatorssh.terminator.domain.repository.SessionRepository
import com.terminatorssh.terminator.domain.service.AuthCryptoHelper
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConnectToCloudUseCase(
    private val userDao: UserDao,
    private val clientFactory: RetrofitClientFactory,
    private val authCryptoHelper: AuthCryptoHelper,
    private val sessionRepository: SessionRepository,
    private val cryptoService: CryptoService
) {
    suspend operator fun invoke(url: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUser() ?: throw IllegalStateException("No local user")

            val keys = authCryptoHelper.deriveKeys(password, user.key_salt, user.auth_salt)

            val calculatedHash = cryptoService.sha256(keys.loginKeyBase64)
            if (calculatedHash != user.login_hash) {
                throw IllegalArgumentException("Invalid Password")
            }

            val api = clientFactory.create(url)

            val registerRequest = RegisterRequest(
                username = user.username,
                authSalt = user.auth_salt,
                keySalt = user.key_salt,
                encryptedMasterKey = user.encrypted_master_key,
                loginKey = keys.loginKeyBase64
            )

            val response = api.register(registerRequest)

            val updatedUser = user.copy(
                server_url = url,
                last_sync_time = null
            )
            userDao.insertUser(updatedUser)

            val currentSession = sessionRepository.getCurrentSession()
            if (currentSession != null) {
                sessionRepository.setCurrentSession(
                    currentSession.copy(token = response.accessToken)
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}