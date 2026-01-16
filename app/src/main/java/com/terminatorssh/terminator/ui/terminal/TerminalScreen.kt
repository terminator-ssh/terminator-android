package com.terminatorssh.terminator.ui.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import jackpal.androidterm.emulatorview.EmulatorView
import org.koin.androidx.compose.koinViewModel

@Composable
fun TerminalScreen(
    hostId: String,
    viewModel: TerminalViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(hostId) {
        viewModel.connect(hostId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val s = state) {
            is TerminalState.Connecting -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            is TerminalState.Error -> {
                Text(
                    text = "Error: ${s.message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is TerminalState.Connected -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        EmulatorView(context, null)
                    },
                    update = { view ->
                        view.attachSession(s.session)

                        val metrics = view.context.resources.displayMetrics
                        view.setDensity(metrics)

                        val fontSizeSp = 5f
                        val fontSizePx = android.util.TypedValue.applyDimension(
                            android.util.TypedValue.COMPLEX_UNIT_SP,
                            fontSizeSp,
                            metrics
                        ).toInt()

                        view.setTextSize(fontSizePx)
                    }
                )
            }
            is TerminalState.Disconnected -> {
                Text("Disconnected", Modifier.align(Alignment.Center))
            }
        }
    }
}