package com.terminatorssh.terminator.data.repository

import android.util.Base64
import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.local.model.UserEntity
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.LoginRequest
import com.terminatorssh.terminator.data.remote.dto.PreflightRequest
import com.terminatorssh.terminator.domain.common.Base64Helper.encodeBase64
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val clientFactory: RetrofitClientFactory,
    private val cryptoService: CryptoService
) : AuthRepository {

    private var currentSession: UserSession? = null

    override suspend fun getCurrentSession(): UserSession? {
        return currentSession
    }

    override suspend fun loginAndSync(
        url: String,
        username: String,
        password: String): Result<UserSession> {

        return withContext(Dispatchers.IO) {
            try {
                val api = clientFactory.create(url)

                val preflightResponse = api.preflight(PreflightRequest(username))

                val kek = cryptoService.deriveKEK(password, preflightResponse.keySalt)

                val loginKeyBytes = cryptoService.deriveKEK(
                    password, preflightResponse.authSalt)
                val loginKeyBase64 = encodeBase64(loginKeyBytes)

                val loginResponse = api.login(LoginRequest(username, loginKeyBase64))

                val encryptedMasterKeyData = cryptoService.unpackBlob(
                    preflightResponse.encryptedMasterKey)
                val masterKeyBytes = cryptoService.decryptAES(
                    encryptedMasterKeyData, kek)

                val loginHash = cryptoService.sha256(loginKeyBase64)

                val userEntity = UserEntity(
                    id = UUID.randomUUID().toString(),
                    username = username,
                    key_salt = preflightResponse.keySalt,
                    auth_salt = preflightResponse.authSalt,
                    encrypted_master_key = preflightResponse.encryptedMasterKey,
                    login_hash = loginHash,
                    server_url = url,
                    last_sync_time = null
                )

                // TODO: multiple users?
                userDao.nukeTable()
                userDao.insertUser(userEntity)

                val session = UserSession(
                    userId = userEntity.id,
                    username = username,
                    masterKey = masterKeyBytes,
                    token = loginResponse.accessToken
                )

                currentSession = session
                Result.success(session)

            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    override suspend fun logout() {
        currentSession = null
        withContext(Dispatchers.IO) {
            userDao.nukeTable()
        }
    }
}