package com.terminatorssh.terminator.domain.repository

import com.terminatorssh.terminator.domain.model.UserSession

interface SessionRepository {
    suspend fun hasUser(): Boolean
    suspend fun getCurrentSession(): UserSession?
    fun setCurrentSession(session: UserSession)
    suspend fun logout()
}