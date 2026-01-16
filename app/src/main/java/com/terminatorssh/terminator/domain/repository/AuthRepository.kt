package com.terminatorssh.terminator.domain.repository

import com.terminatorssh.terminator.domain.model.UserSession

interface AuthRepository {
    suspend fun getCurrentSession(): UserSession?

    suspend fun login(url: String, username: String, password: String): Result<UserSession>

    suspend fun logout()
}