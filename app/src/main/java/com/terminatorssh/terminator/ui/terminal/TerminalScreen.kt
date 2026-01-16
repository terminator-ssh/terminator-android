package com.terminatorssh.terminator.ui.terminal

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TerminalScreen(hostId: String) {
    Column {
        Text("dummy dum")
        Text(hostId)
    }
}