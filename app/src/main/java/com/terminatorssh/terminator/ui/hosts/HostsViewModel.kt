package com.terminatorssh.terminator.ui.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terminatorssh.terminator.domain.common.SyncConstants
import com.terminatorssh.terminator.domain.repository.HostRepository
import com.terminatorssh.terminator.domain.repository.SyncRepository
import com.terminatorssh.terminator.ui.common.AnimationConstants
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HostsViewModel(
    private val hostRepository: HostRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        startAutoSync()
    }

    private fun startAutoSync() {
        viewModelScope.launch {
            while (true) {
                performSyncWithAnimation()

                delay(SyncConstants.AUTO_SYNC_DELAY)
            }
        }
    }

    val state: StateFlow<HostsState> = hostRepository.getHostsFlow()
        .map { list ->
            if (list.isEmpty())
                HostsState.Empty
            else
                HostsState.Success(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HostsState.Loading
        )

    fun refresh() {
        viewModelScope.launch {
            performSyncWithAnimation()
        }
    }

    suspend fun performSyncWithAnimation() {
        if (_isSyncing.value) return

        _isSyncing.value = true

        val syncJob = viewModelScope.async {
            syncRepository.sync()
        }

        val animationJob = viewModelScope.async {
            delay(AnimationConstants.MINIMUM_SYNC_SPINNER_TIME)
        }

        val results = awaitAll(syncJob, animationJob)

        //val syncResult = results[0] as Result<Unit>

        _isSyncing.value = false
    }
}