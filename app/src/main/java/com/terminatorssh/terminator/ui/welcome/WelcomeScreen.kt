package com.terminatorssh.terminator.ui.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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
            // Effectively invisible as we navigate away,
            // but you could put a "Get Started" button here if you wanted.
        }
    }
}

@Composable
fun UnlockView(
    error: String?,
    onUnlock: (String) -> Unit,
    onWipe: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showWipeConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Master Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onUnlock(password) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Unlock Vault")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { showWipeConfirm = true }) {
            Text("Forgot Password? Reset App", color = MaterialTheme.colorScheme.error)
        }
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("Reset App?") },
            text = { Text("This will delete all local data. You will need to re-connect to your server.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onWipe()
                        showWipeConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Wipe Data") }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) { Text("Cancel") }
            }
        )
    }
}