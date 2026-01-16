package com.terminatorssh.terminator.data.remote.service

import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.local.model.UserEntity
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class UserPersistenceService(
    private val userDao: UserDao,
    private val blobDao: BlobDao,
    private val sessionRepository: SessionRepository
) {
    /**
     * NOTE: NUKES DATABASE TABLES!
     */
    suspend fun saveAndActivateNewSession(
        username: String,
        keySalt: String,
        authSalt: String,
        encryptedMasterKey: String,
        loginHash: String,
        serverUrl: String?,
        masterKeyBytes: ByteArray,
        token: String?
    ) : Result<UserSession>
    = withContext(Dispatchers.IO) {
        val userId = UUID.randomUUID().toString()

        val userEntity = UserEntity(
            id = userId,
            username = username,
            key_salt = keySalt,
            auth_salt = authSalt,
            encrypted_master_key = encryptedMasterKey,
            login_hash = loginHash,
            server_url = serverUrl,
            last_sync_time = null
        )

        userDao.nukeTable()
        blobDao.nukeTable()

        userDao.insertUser(userEntity)

        val session = UserSession(
            userId = userId,
            username = username,
            masterKey = masterKeyBytes,
            token = token
        )
        sessionRepository.setCurrentSession(session)
        return@withContext Result.success(session)
    }
}