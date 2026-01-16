package com.terminatorssh.terminator.domain.repository

import com.terminatorssh.terminator.domain.model.Host
import kotlinx.coroutines.flow.Flow

interface HostRepository {
    fun getHostsFlow(): Flow<List<Host>>
    suspend fun findHost(id: String): Host?
    suspend fun saveHost(host: Host)
    suspend fun deleteHost(hostId: String)
}