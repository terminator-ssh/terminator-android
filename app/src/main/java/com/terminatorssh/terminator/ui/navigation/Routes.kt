package com.terminatorssh.terminator.ui.navigation

import com.terminatorssh.terminator.ui.hosts.form.HostFormAction
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object HostsRoute

@Serializable
data class TerminalRoute(val hostId: String)

@Serializable
data class HostFormRoute(val hostId: String?)