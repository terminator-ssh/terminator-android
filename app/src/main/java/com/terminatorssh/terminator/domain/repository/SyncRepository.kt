package com.terminatorssh.terminator.domain.repository

interface SyncRepository {
    suspend fun sync(): Result<Unit>
}