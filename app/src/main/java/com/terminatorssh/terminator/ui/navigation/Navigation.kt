package com.terminatorssh.terminator.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.terminatorssh.terminator.ui.login.LoginScreen
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
            // TODO hosts screen placeholder
            Text("Login Successful!")
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