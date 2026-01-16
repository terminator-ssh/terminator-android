package com.terminatorssh.terminator.data.repository

import com.google.gson.Gson
import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.local.model.BlobEntity
import com.terminatorssh.terminator.domain.model.Host
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList

class HostRepositoryImpl(
    private val blobDao: BlobDao,
    private val authRepository: AuthRepository,
    private val cryptoService: CryptoService,
    private val gson: Gson
) : HostRepository {

    override fun getHostsFlow(): Flow<List<Host>> {
        return blobDao.getAllActiveBlobsFlow()
            .map { blobs ->
                val session = authRepository.getCurrentSession()
                    ?: return@map emptyList<Host>()

                blobs.mapNotNull { blobEntity ->
                    try {
                        decryptBlob(blobEntity, session)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun findHost(id: String): Host? {
        return withContext(Dispatchers.IO) {
            val session = authRepository.getCurrentSession() ?: return@withContext null
            val blobEntity = blobDao.getBlobById(id) ?: return@withContext null

            try {
                decryptBlob(blobEntity, session)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun saveHost(host: Host) {
        withContext(Dispatchers.IO) {
            val session = authRepository.getCurrentSession()
                ?: throw IllegalStateException("Not logged in")

            val jsonString = gson.toJson(host)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            val encryptedData = cryptoService.encryptAES(jsonBytes, session.masterKey)

            val packedBlob = cryptoService.packBlob(encryptedData)

            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            val entity = BlobEntity(
                id = host.id,
                blob = packedBlob,
                iv = encryptedData.iv,
                updated_at = now,
                is_deleted = false
            )

            blobDao.insertBlobs(listOf(entity))
        }
    }

    override suspend fun deleteHost(hostId: String) {
        withContext(Dispatchers.IO) {
            val existing = blobDao.getBlobById(hostId) ?: return@withContext

            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            val deletedEntity = existing.copy(
                is_deleted = true,
                updated_at = now
            )
            blobDao.insertBlobs(listOf(deletedEntity))
        }
    }

    private fun decryptBlob(blobEntity: BlobEntity, session: UserSession): Host {
        val encryptedData = cryptoService.unpackBlob(blobEntity.blob)
        val jsonBytes = cryptoService.decryptAES(encryptedData, session.masterKey)
        val jsonString = String(jsonBytes, Charsets.UTF_8)
        val host = gson.fromJson(jsonString, Host::class.java)
        return host.copy(id = blobEntity.id)
    }
}