package com.terminatorssh.terminator.data.usecase

import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.LoginRequest
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.repository.SessionRepository
import com.terminatorssh.terminator.domain.service.CryptoService
import com.terminatorssh.terminator.domain.service.AuthCryptoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnlockVaultUseCase(
    private val userDao: UserDao,
    private val authCryptoHelper: AuthCryptoHelper,
    private val cryptoService: CryptoService,
    private val clientFactory: RetrofitClientFactory,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userEntity = userDao.getUser()
                ?: return@withContext Result.failure(Exception("No local user found."))

            val keys = authCryptoHelper.deriveKeys(password, userEntity.key_salt, userEntity.auth_salt)

            val calculatedHash = cryptoService.sha256(keys.loginKeyBase64)
            if (calculatedHash != userEntity.login_hash) {
                return@withContext Result.failure(Exception("Invalid Password"))
            }

            val encryptedMasterKey = cryptoService.unpackBlob(userEntity.encrypted_master_key)
            val masterKeyBytes = cryptoService.decryptAES(encryptedMasterKey, keys.kek)

            var accessToken: String? = null
            if (userEntity.server_url != null) {
                try {
                    val api = clientFactory.create(userEntity.server_url)
                    val loginRes = api.login(LoginRequest(userEntity.username, keys.loginKeyBase64))
                    accessToken = loginRes.accessToken
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val session = UserSession(
                userId = userEntity.id,
                username = userEntity.username,
                masterKey = masterKeyBytes,
                token = accessToken
            )
            sessionRepository.setCurrentSession(session)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}