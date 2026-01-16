package com.terminatorssh.terminator.data.repository

import android.util.Log
import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.data.local.model.UserEntity
import com.terminatorssh.terminator.data.mapper.SyncMapper
import com.terminatorssh.terminator.data.remote.RetrofitClientFactory
import com.terminatorssh.terminator.data.remote.dto.SyncRequest
import com.terminatorssh.terminator.domain.repository.SessionRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean

class SyncRepositoryImpl(
    private val sessionRepository: SessionRepository,
    private val userDao: UserDao,
    private val blobDao: BlobDao,
    private val clientFactory: RetrofitClientFactory,
    private val syncMapper: SyncMapper
) : SyncRepository {

    private val _isSyncing = AtomicBoolean(false)

    override suspend fun sync(): Result<Unit> = withContext(Dispatchers.IO) {
        if (_isSyncing.get()) {
            Log.d("SYNC", "Not syncing due to syncing")
            return@withContext Result.success(Unit) }

        try {
            _isSyncing.set(true)
            Log.d("SYNC", "Starting Sync...")

            val session = sessionRepository.getCurrentSession()
                ?: return@withContext Result.failure(
                    IllegalStateException("Not logged in"))

            val user = userDao.getUser()
                ?: return@withContext Result.failure(
                    IllegalStateException("Local user state missing"))

            if(user.server_url == null) {
                Log.d("SYNC", "Not syncing due to server url")
                _isSyncing.set(false)
                return@withContext Result.success(Unit) //offline
            }
            val server_url = user.server_url
            val lastSyncTime = user.last_sync_time ?: Instant.EPOCH.toString()

            Log.d("SYNC", "Last sync time: $lastSyncTime")
            Log.d("SYNC", "Connecting to: $server_url")

            val changedBlobs = blobDao.getBlobsModifiedSince(lastSyncTime)

            val pushPayload = changedBlobs.map { syncMapper.toDto(it) }

            Log.d("SYNC", "Pushing ${changedBlobs.size} local changes")

            val api = clientFactory.create(server_url)
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
                val incomingEntities = response.blobs.map { syncMapper.toEntity(it) }
                blobDao.insertBlobs(incomingEntities)
            }

            Log.d("SYNC", "Server returned ${response.blobs.size} updates")

            updateUserLastSyncTime(user, response.syncTime)

            Log.d("SYNC", "Sync Complete. New sync time: ${response.syncTime}")

            _isSyncing.set(false)
            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()

            _isSyncing.set(false)
            Result.failure(e)
        }
    }

    private suspend fun updateUserLastSyncTime(user: UserEntity, newTime: String) {
        val serverTime = try {
            Instant.parse(newTime)
        } catch (e: Exception) {
            Instant.now()
        }

        // clock skew VERY IMPORTANT
        val safeTime = serverTime.minus(10, ChronoUnit.MINUTES)

        val updatedUser = user.copy(last_sync_time = safeTime.toString())
        userDao.insertUser(updatedUser)
    }
}