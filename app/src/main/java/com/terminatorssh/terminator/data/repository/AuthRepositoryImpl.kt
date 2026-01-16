package com.terminatorssh.terminator.data.repository

import com.terminatorssh.terminator.data.local.dao.BlobDao
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
    private val blobDao: BlobDao,
    private val clientFactory: RetrofitClientFactory,
    private val cryptoService: CryptoService
) : AuthRepository {

    private var currentSession: UserSession? = null

    override suspend fun doesUserExist(): Boolean {
        return userDao.getUser() != null
    }

    override suspend fun getCurrentSession(): UserSession? {
        return currentSession
    }

    override suspend fun login(
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
                blobDao.nukeTable()
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

    override suspend fun unlockVault(password: String): Result<UserSession> {
        return withContext(Dispatchers.IO) {
            try {
                val userEntity = userDao.getUser()
                    ?: return@withContext Result.failure(Exception("No local user found."))

                val kek = cryptoService.deriveKEK(password, userEntity.key_salt)
                val loginKeyBytes = cryptoService.deriveKEK(password, userEntity.auth_salt)
                val loginKeyBase64 = encodeBase64(loginKeyBytes)

                val calculatedHash = cryptoService.sha256(loginKeyBase64)
                if (calculatedHash != userEntity.login_hash) {
                    return@withContext Result.failure(Exception("Invalid Password"))
                }

                val encryptedData = cryptoService.unpackBlob(userEntity.encrypted_master_key)
                val masterKeyBytes = cryptoService.decryptAES(encryptedData, kek)

                var accessToken: String? = null

                if (userEntity.server_url != null) {
                    try {
                        val api = clientFactory.create(userEntity.server_url)
                        val loginResponse = api.login(
                            LoginRequest(userEntity.username, loginKeyBase64)
                        )
                        accessToken = loginResponse.accessToken
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
            blobDao.nukeTable()
        }
    }

    override suspend fun createLocalUser(username: String, password: String): Result<UserSession> {
        return withContext(Dispatchers.IO) {
            try {
                val keySalt = cryptoService.generateRandomBytes(16)
                val authSalt = cryptoService.generateRandomBytes(16)
                val masterKey = cryptoService.generateRandomBytes(32)

                val keySaltB64 = encodeBase64(keySalt)
                val authSaltB64 = encodeBase64(authSalt)

                val kek = cryptoService.deriveKEK(password, keySaltB64)

                val loginKeyBytes = cryptoService.deriveKEK(password, authSaltB64)
                val loginKeyB64 = encodeBase64(loginKeyBytes)
                val loginHash = cryptoService.sha256(loginKeyB64)

                val encryptedData = cryptoService.encryptAES(masterKey, kek)
                val packedMK = cryptoService.packBlob(encryptedData)

                val userId = UUID.randomUUID().toString()

                val userEntity = UserEntity(
                    id = userId,
                    username = username,
                    key_salt = keySaltB64,
                    auth_salt = authSaltB64,
                    encrypted_master_key = packedMK,
                    login_hash = loginHash,
                    server_url = null,
                    last_sync_time = null
                )

                userDao.nukeTable()
                blobDao.nukeTable()
                userDao.insertUser(userEntity)

                val session = UserSession(
                    userId = userId,
                    username = username,
                    masterKey = masterKey,
                    token = null
                )

                currentSession = session
                Result.success(session)

            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}