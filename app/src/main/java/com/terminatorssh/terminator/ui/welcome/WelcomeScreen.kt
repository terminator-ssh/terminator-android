package com.terminatorssh.terminator.ui.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun WelcomeScreen(
    onNavigateToConnect: () -> Unit,
    onUnlockSuccess: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val error by viewModel.unlockError.collectAsState()

    LaunchedEffect(state) {
        if (state is WelcomeState.NoUser) {
            onNavigateToConnect()
        }
    }

    when (val s = state) {
        is WelcomeState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WelcomeState.HasUser -> {
            UnlockView(
                error = error,
                onUnlock = { pass -> viewModel.unlock(pass, onUnlockSuccess) },
                onWipe = { viewModel.wipeData() }
            )
        }
        is WelcomeState.NoUser -> {
            // handled by launch effect
        }
    }
}