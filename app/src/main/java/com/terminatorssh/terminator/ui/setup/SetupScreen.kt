package com.terminatorssh.terminator.ui.setup

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: SetupViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // nav
    LaunchedEffect(state) {
        if (state is SetupState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    LoginContent(
        state = state,
        onConnectClick = { url, user, pass ->
            viewModel.connect(url, user, pass)
        },
        onCreateLocalClick = { user, pass ->
            viewModel.createLocal(user, pass)
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            state = SetupState.Idle,
            onConnectClick = { _, _, _ -> },
            onCreateLocalClick = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginErrorPreview() {
    MaterialTheme {
        LoginContent(
            state = SetupState.Error("Invalid credentials"),
            onConnectClick = { _, _, _ -> },
            onCreateLocalClick = { _, _ -> }
        )
    }
}