package com.root2rise.bookkeeping.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.root2rise.bookkeeping.data.TransactionEntity
import com.root2rise.bookkeeping.ui.components.AppBottomNavigation
import com.root2rise.bookkeeping.ui.screen.DashboardScreen
import com.root2rise.bookkeeping.ui.screen.LoginScreen
import com.root2rise.bookkeeping.ui.screen.ProfileScreen
import com.root2rise.bookkeeping.ui.screen.RegisterScreen
import com.root2rise.bookkeeping.ui.screen.TransactionsScreen
import com.root2rise.bookkeeping.ui.screen.WalletScreen
import com.root2rise.bookkeeping.ui.screen.WelcomeScreen
import com.root2rise.bookkeeping.viewmodel.BookkeepingViewModel

@Composable
fun AppNavigation(
    viewModel: BookkeepingViewModel,
    onStartVoiceInput: () -> Unit,
    onStartModificationVoiceInput: (TransactionEntity) -> Unit,
    speaker: (String) -> Unit
) {
    val navController = rememberNavController()
    var isAuthenticated by remember { mutableStateOf(false) }
    var showWelcome by remember { mutableStateOf(true) }

    if (!isAuthenticated) {
        // Authentication Flow
        NavHost(
            navController = navController,
            startDestination = if (showWelcome) Screen.Welcome.route else Screen.Login.route
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = {
                        showWelcome = false
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        isAuthenticated = true
                        // Navigation to home happens automatically when isAuthenticated changes
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        isAuthenticated = true
                        // Navigation to home happens automatically when isAuthenticated changes
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }
    } else {
        // Main App Flow with Bottom Navigation
        MainAppNavigation(
            viewModel = viewModel,
            onStartVoiceInput = onStartVoiceInput,
            onStartModificationVoiceInput = onStartModificationVoiceInput,
            speaker = speaker,
            onLogout = {
                isAuthenticated = false
                showWelcome = false
            }
        )
    }
}

@Composable
private fun MainAppNavigation(
    viewModel: BookkeepingViewModel,
    onStartVoiceInput: () -> Unit,
    onStartModificationVoiceInput: (TransactionEntity) -> Unit,
    speaker: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that should show bottom navigation
    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Wallet.route,
        Screen.Transactions.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                AppBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onStartVoiceInput = onStartVoiceInput,
                        onStartModificationVoiceInput = onStartModificationVoiceInput,
                        speaker = speaker
                    )
                }

                composable(Screen.Wallet.route) {
                    WalletScreen(viewModel = viewModel)
                }

                composable(Screen.Transactions.route) {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onStartModificationVoiceInput = onStartModificationVoiceInput,
                        speaker = speaker
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}
