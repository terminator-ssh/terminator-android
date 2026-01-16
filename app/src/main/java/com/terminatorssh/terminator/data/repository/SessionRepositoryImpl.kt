package com.terminatorssh.terminator.data.repository

import com.terminatorssh.terminator.data.local.dao.BlobDao
import com.terminatorssh.terminator.data.local.dao.UserDao
import com.terminatorssh.terminator.domain.model.UserSession
import com.terminatorssh.terminator.domain.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val userDao: UserDao,
    private val blobDao: BlobDao
) : SessionRepository {

    private var memorySession: UserSession? = null

    override suspend fun hasUser(): Boolean = userDao.getUser() != null

    override suspend fun getCurrentSession(): UserSession? = memorySession

    override fun setCurrentSession(session: UserSession) {
        memorySession = session
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        memorySession = null
        userDao.nukeTable()
        blobDao.nukeTable()
    }
}