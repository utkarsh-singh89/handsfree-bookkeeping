package com.root2rise.bookkeeping.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object Profile : Screen("profile")
    object Transactions : Screen("transactions")
    object Settings : Screen("settings")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", "home")
    object Wallet : BottomNavItem(Screen.Wallet.route, "Wallet", "wallet")
    object Transactions : BottomNavItem(Screen.Transactions.route, "Transactions", "list")
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", "person")
}

fun getBottomNavItems() = listOf(
    BottomNavItem.Home,
    BottomNavItem.Wallet,
    BottomNavItem.Transactions,
    BottomNavItem.Profile
)
