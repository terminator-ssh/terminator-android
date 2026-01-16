package com.terminatorssh.terminator.data.repository

import com.google.gson.Gson
import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.domain.model.Host
import com.terminatorssh.terminator.domain.repository.AuthRepository
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.service.CryptoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HostRepositoryImpl(
    private val blobDao: BlobDao,
    private val authRepository: AuthRepository,
    private val cryptoService: CryptoService,
    private val gson: Gson
) : HostRepository {

    override suspend fun getHosts(): List<Host> {
        return withContext(Dispatchers.IO) {
            val session = authRepository.getCurrentSession()
                ?: return@withContext emptyList()

            val blobs = blobDao.getAllActiveBlobs()

            blobs.mapNotNull { blobEntity ->
                try {
                    val encryptedData = cryptoService.unpackBlob(blobEntity.blob)

                    val jsonBytes = cryptoService.decryptAES(encryptedData, session.masterKey)
                    val jsonString = String(jsonBytes, Charsets.UTF_8)

                    val host = gson.fromJson(jsonString, Host::class.java)

                    return@mapNotNull host.copy(id = blobEntity.id)
                } catch (e: Exception) {
                    e.printStackTrace() // TODO: something more robust?
                    return@mapNotNull null
                }
            }
        }
    }
}