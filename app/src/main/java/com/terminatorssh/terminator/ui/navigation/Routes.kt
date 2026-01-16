package com.terminatorssh.terminator.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HostsRoute

@Serializable
data class TerminalRoute(val hostId: String)