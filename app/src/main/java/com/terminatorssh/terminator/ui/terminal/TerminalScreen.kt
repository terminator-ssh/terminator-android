package com.terminatorssh.terminator.ui.terminal

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import jackpal.androidterm.emulatorview.EmulatorView
import org.koin.androidx.compose.koinViewModel

@Composable
fun TerminalScreen(
    hostId: String,
    viewModel: TerminalViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var terminalView by remember { mutableStateOf<EmulatorView?>(null) }
    val context = LocalContext.current

    LaunchedEffect(hostId) {
        viewModel.connect(hostId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    fun toggleKeyboard() {
        terminalView?.let { view ->
            view.requestFocus()
            val inputMethodManager = context.getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager

            //val insets = ViewCompat.getRootWindowInsets(view)
            //val isKeyboardVisible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
            //if(isKeyboardVisible) {
            //    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            //} else {
            //    view.requestFocus() // important line
            //    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            //}

            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state is TerminalState.Connected) {
                FloatingActionButton(onClick = { toggleKeyboard() }) {
                    Icon(Icons.Default.Keyboard, contentDescription = "Show Keyboard")
                }
            }
        }
    ) { padding ->
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
                            EmulatorView(context, null).apply {
                                isFocusable = true
                                isFocusableInTouchMode = true
                            }
                        },
                        update = { view ->
                            terminalView = view

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

                            view.requestFocus()
                        }
                    )
                }

                is TerminalState.Disconnected -> {
                    Text("Disconnected", Modifier.align(Alignment.Center))
                }
            }
        }
    }
}