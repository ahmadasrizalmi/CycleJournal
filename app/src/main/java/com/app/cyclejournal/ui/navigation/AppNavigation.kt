package com.app.cyclejournal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.cyclejournal.ui.home.CycleHomeScreen
import com.app.cyclejournal.ui.home.CycleViewModel
import com.app.cyclejournal.ui.onboarding.OnboardingScreen
import com.app.cyclejournal.ui.onboarding.OnboardingViewModel
import com.app.cyclejournal.ui.report.ReportScreen
import com.app.cyclejournal.ui.settings.SettingsScreen
import com.app.cyclejournal.ui.settings.SettingsViewModel

@Composable
fun AppNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val cycleViewModel: CycleViewModel = hiltViewModel()
            CycleHomeScreen(
                viewModel = cycleViewModel,
                onNavigateToReport = {
                    navController.navigate(Screen.Report.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Report.route) {
            val cycleViewModel: CycleViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            ReportScreen(
                cycleViewModel = cycleViewModel,
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
