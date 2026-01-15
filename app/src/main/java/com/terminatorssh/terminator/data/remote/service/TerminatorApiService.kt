package com.terminatorssh.terminator.data.remote.service

import com.terminatorssh.terminator.data.remote.dto.*
import retrofit2.http.*

interface TerminatorApiService {
    @POST(value = "auth/preflight")
    suspend fun preflight(@Body req: PreflightRequest): PreflightResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse

    @POST("sync")
    suspend fun sync(
        @Header("Authorization") token: String,
        @Body req: SyncRequest): SyncResponse
}