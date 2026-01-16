package com.terminatorssh.terminator.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SyncRepository {
    suspend fun sync(): Result<Unit>
}