package com.app.cyclejournal

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.security.BiometricAuthHelper
import com.app.cyclejournal.security.SecurityPinManager
import com.app.cyclejournal.ui.navigation.AppNavHost
import com.app.cyclejournal.ui.navigation.Screen
import com.app.cyclejournal.ui.security.PinLockScreen
import com.app.cyclejournal.ui.theme.CycleJournalTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var pinManager: SecurityPinManager

    @Inject
    lateinit var prefs: OnboardingPreferences

    private var lastBackgroundTimestamp = 0L
    private val isAppUnlocked = androidx.compose.runtime.mutableStateOf(false)
    private lateinit var biometricAuthHelper: BiometricAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Android 12+ Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enforce medical privacy: block screenshots and hide preview in Recents
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        biometricAuthHelper = BiometricAuthHelper(this)

        // If PIN is not set yet (first-time install), start unlocked
        if (!pinManager.isPinSet() || !prefs.isOnboardingCompleted()) {
            isAppUnlocked.value = true
        }

        // ProcessLifecycleOwner 30-second background lock guard
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                lastBackgroundTimestamp = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
                if (elapsed > 30_000L && pinManager.isPinSet() && prefs.isOnboardingCompleted()) {
                    isAppUnlocked.value = false
                }
            }
        })

        setContent {
            CycleJournalTheme {
                if (!isAppUnlocked.value && pinManager.isPinSet() && prefs.isOnboardingCompleted()) {
                    PinLockScreen(
                        onPinEntered = { enteredPin ->
                            val valid = pinManager.verifyPin(enteredPin)
                            if (valid) {
                                isAppUnlocked.value = true
                            }
                            valid
                        },
                        onBiometricRequested = {
                            if (biometricAuthHelper.canAuthenticate()) {
                                biometricAuthHelper.promptBiometric(
                                    onSuccess = { isAppUnlocked.value = true },
                                    onErrorOrFallback = {}
                                )
                            }
                        }
                    )
                } else {
                    val startDestination = if (prefs.isOnboardingCompleted()) {
                        Screen.Home.route
                    } else {
                        Screen.Onboarding.route
                    }
                    AppNavHost(startDestination = startDestination)
                }
            }
        }
    }
}
