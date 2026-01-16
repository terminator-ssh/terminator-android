package com.terminatorssh.terminator.data.repository

import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.local.model.BlobEntity
import com.terminatorssh.terminator.data.local.model.UserEntity
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.EncryptedBlobDto
import com.terminatorssh.terminator.data.remote.dto.SyncRequest
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class SyncRepositoryImpl(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val blobDao: BlobDao,
    private val clientFactory: RetrofitClientFactory
) : SyncRepository {

    override suspend fun sync(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val session = authRepository.getCurrentSession()
                ?: return@withContext Result.failure(
                    IllegalStateException("Not logged in"))

            val user = userDao.getUser()
                ?: return@withContext Result.failure(
                    IllegalStateException("Local user state missing"))

            val serverUrl = user.server_url
                ?: return@withContext Result.failure(
                    IllegalStateException("No server URL configured"))

            val lastSyncTime = user.last_sync_time ?: Instant.EPOCH.toString()

            val changedBlobs = blobDao.getBlobsModifiedSince(lastSyncTime)

            val pushPayload = changedBlobs.map { entity ->
                EncryptedBlobDto(
                    id = entity.id,
                    blob = entity.blob,
                    iv = entity.iv,
                    updatedAt = entity.updated_at,
                    isDeleted = entity.is_deleted
                )
            }

            val api = clientFactory.create(serverUrl)
            val authHeader = "Bearer ${session.token}"

            val response = api.sync(
                token = authHeader,
                req = SyncRequest(
                    blobs = pushPayload,
                    lastSyncTime = lastSyncTime,
                    userId = user.id
                )
            )

            if (response.blobs.isNotEmpty()) {
                val incomingEntities = response.blobs.map { dto ->
                    BlobEntity(
                        id = dto.id,
                        blob = dto.blob,
                        iv = dto.iv,
                        updated_at = dto.updatedAt,
                        is_deleted = dto.isDeleted
                    )
                }
                blobDao.insertBlobs(incomingEntities)
            }

            updateUserLastSyncTime(user, response.syncTime)

            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun updateUserLastSyncTime(user: UserEntity, newTime: String) {
        val updatedUser = user.copy(last_sync_time = newTime)
        userDao.insertUser(updatedUser)
    }
}