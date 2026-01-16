package com.terminatorssh.terminator.ui.hosts

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.terminatorssh.terminator.domain.model.Host
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostsScreen(
    onHostClick: (Host) -> Unit,
    onHostAddClick: () -> Unit,
    onHostEditClick: (Host) -> Unit,
    onLogout: () -> Unit,
    viewModel: HostsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val isSyncing by viewModel.isSyncing.collectAsState()

    val hasSyncError by viewModel.hasSyncError.collectAsState()

    val iconTint = when {
        // TODO: something better than mimicking the default disabled color?
        isSyncing -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        hasSyncError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    var showMenu by remember { mutableStateOf(false) }
    var showConnectDialog by remember { mutableStateOf(false) }
    var showWipeDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HostsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true
                    )
                }
                is HostsEvent.NavigateToWelcome -> {
                    onLogout()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Hosts") },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isSyncing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.rotate(if (isSyncing) angle else 0f),
                            tint = iconTint
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Connect to Cloud") },
                                onClick = { showMenu = false; showConnectDialog = true },
                                leadingIcon = { Icon(Icons.Default.CloudUpload, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Wipe Data") },
                                onClick = { showMenu = false; showWipeDialog = true },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onHostAddClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Host")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is HostsState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is HostsState.Empty -> {
                    Text(
                        "No hosts found.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HostsState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(s.hosts) { host ->
                            HostItem(
                                host,
                                onClick = { onHostClick(host) },
                                onEditClick = { onHostEditClick(host) })
                        }
                    }
                }
            }
        }

        if (showWipeDialog) {
            AlertDialog(
                onDismissRequest = { showWipeDialog = false },
                title = { Text("Nuke Everything?") },
                text = { Text("This will permanently delete all local data. This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.wipeData()
                            showWipeDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Wipe") }
                },
                dismissButton = {
                    TextButton(onClick = { showWipeDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showConnectDialog) {
            ConnectServerDialog(
                onDismiss = { showConnectDialog = false },
                onConnect = { url, pass ->
                    viewModel.connectToCloud(url, pass)
                    showConnectDialog = false
                }
            )
        }
    }
}