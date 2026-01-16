package com.terminatorssh.terminator.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    // nav
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
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

@Composable
fun LoginContent(
    state: LoginState,
    onLoginClick: (String, String, String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit,
    onCreateLocalClick: (String, String) -> Unit
) {
    var isLocalMode by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // defaults
    var serverUrl by remember { mutableStateOf("http://192.168.100.10:5000/api/v1") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Terminator",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { isLocalMode = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(!isLocalMode) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud Restore")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { isLocalMode = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isLocalMode) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Vault")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isLocalMode) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state is LoginState.Error) {
                Text(
                    text = state.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state is LoginState.Loading) {
                CircularProgressIndicator()
            } else {
                val buttonText = when {
                    isLocalMode -> "Create Local Vault"
                    isRegisterMode -> "Sign Up"
                    else -> "Connect & Restore"
                }

                Button(
                    onClick = {
                        when {
                            isLocalMode -> onCreateLocalClick(username, password)
                            isRegisterMode -> onRegisterClick(serverUrl, username, password)
                            else -> onLoginClick(serverUrl, username, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(buttonText)
                }
            }

            if (!isLocalMode && state !is LoginState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                    Text(
                        text = if (isRegisterMode) "Already have an account? Login"
                               else "Need an account? Register",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginContent(
            state = LoginState.Idle,
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
            state = LoginState.Error("Invalid credentials"),
            onLoginClick = { _, _, _ -> },
            onCreateLocalClick = { _, _ -> },
            onRegisterClick = { _, _, _ -> }
        )
    }
}