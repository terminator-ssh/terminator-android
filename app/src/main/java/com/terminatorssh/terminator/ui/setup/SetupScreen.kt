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
        onLoginClick = { url, user, pass ->
            viewModel.login(url, user, pass)
        },
        onCreateLocalClick = { user, pass ->
            viewModel.createLocal(user, pass)
        },
        onRegisterClick = { url, user, pass ->
            viewModel.register(url, user, pass)
        },
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            state = SetupState.Idle,
            onLoginClick = { _, _, _ -> },
            onCreateLocalClick = { _, _ -> },
            onRegisterClick = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginErrorPreview() {
    MaterialTheme {
        LoginContent(
            state = SetupState.Error("Invalid credentials"),
            onLoginClick = { _, _, _ -> },
            onCreateLocalClick = { _, _ -> },
            onRegisterClick = { _, _, _ -> }
        )
    }
}