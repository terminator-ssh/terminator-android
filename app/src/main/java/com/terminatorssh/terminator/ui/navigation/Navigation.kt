package com.terminatorssh.terminator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.terminatorssh.terminator.ui.hosts.HostsScreen
import com.terminatorssh.terminator.ui.hosts.form.HostFormAction
import com.terminatorssh.terminator.ui.hosts.form.HostFormScreen
import com.terminatorssh.terminator.ui.login.LoginScreen
import com.terminatorssh.terminator.ui.terminal.TerminalScreen
import com.terminatorssh.terminator.ui.theme.TerminatorTheme

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = LoginRoute) {

        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HostsRoute) {
                        // prevent user from going back to login
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<HostsRoute> {
            HostsScreen(
                onHostClick = { host ->
                    navController.navigate(TerminalRoute(host.id))
                },
                onHostEditClick = { host ->
                    navController.navigate(HostFormRoute(host.id))
                },
                onHostAddClick = {
                    navController.navigate(HostFormRoute(null))
                }
            )
        }

        composable<TerminalRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TerminalRoute>()

            TerminalScreen(hostId = route.hostId)
        }

        composable<HostFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HostFormRoute>()
            val action =
                if (route.hostId != null)
                    HostFormAction.Edit(route.hostId)
                else
                    HostFormAction.New

            HostFormScreen(
                action = action,
                onNavigateBack = {
                    navController.popBackStack()
                })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationPreview() {
    TerminatorTheme {
        Navigation()
    }
}