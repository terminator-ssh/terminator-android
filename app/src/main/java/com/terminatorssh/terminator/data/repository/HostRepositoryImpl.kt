package com.terminatorssh.terminator.data.repository

import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.mapper.HostDataMapper
import com.terminatorssh.terminator.domain.model.Host
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter

class HostRepositoryImpl(
    private val blobDao: BlobDao,
    private val sessionRepository: SessionRepository,
    private val hostMapper: HostDataMapper
) : HostRepository {

    override fun getHostsFlow(): Flow<List<Host>> {
        return blobDao.getAllActiveBlobsFlow()
            .map { blobs ->
                val session = sessionRepository.getCurrentSession()
                    ?: return@map emptyList<Host>()

                blobs.mapNotNull { entity ->
                    hostMapper.mapToHost(entity, session.masterKey)
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun findHost(id: String): Host? = withContext(Dispatchers.IO) {
        val session = sessionRepository.getCurrentSession() ?: return@withContext null
        val entity = blobDao.getBlobById(id) ?: return@withContext null

        return@withContext hostMapper.mapToHost(entity, session.masterKey)
    }

    override suspend fun saveHost(host: Host) = withContext(Dispatchers.IO) {
        val session = sessionRepository.getCurrentSession()
            ?: throw IllegalStateException("Not logged in")

        val entity = hostMapper.mapToEntity(host, session.masterKey)
        blobDao.insertBlobs(listOf(entity))
    }

    override suspend fun deleteHost(hostId: String) = withContext(Dispatchers.IO) {
        val existing = blobDao.getBlobById(hostId) ?: return@withContext

        val deletedEntity = existing.copy(
            is_deleted = true,
            updated_at = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
        blobDao.insertBlobs(listOf(deletedEntity))
    }
}