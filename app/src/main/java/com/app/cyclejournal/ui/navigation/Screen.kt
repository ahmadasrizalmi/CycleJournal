package com.app.cyclejournal.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Report : Screen("report")
    object Settings : Screen("settings")
}
