package com.terminatorssh.terminator.domain.repository

import com.terminatorssh.terminator.domain.model.Host

interface HostRepository {
    suspend fun getHosts(): List<Host>
}