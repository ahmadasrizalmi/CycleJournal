package com.app.cyclejournal.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cyclejournal.data.local.dao.CycleDao
import com.app.cyclejournal.data.local.dao.DailyLogDao
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.security.SecurityPinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class OnboardingStep {
    DISCLAIMER,
    PIN_SETUP,
    PIN_CONFIRM,
    BASELINE_CYCLE,
    RECOVERY_KEY_DISPLAY
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val pinManager: SecurityPinManager,
    private val prefs: OnboardingPreferences,
    private val cycleDao: CycleDao,
    private val dailyLogDao: DailyLogDao
) : ViewModel() {

    private val _currentStep = MutableStateFlow(OnboardingStep.DISCLAIMER)
    val currentStep: StateFlow<OnboardingStep> = _currentStep

    var tempPin: String = ""
    val anonymousId: String = pinManager.getOrCreateAnonymousUserId()

    fun nextStep(step: OnboardingStep) {
        _currentStep.value = step
    }

    fun savePin(pin: String) {
        pinManager.savePin(pin)
    }

    fun finalizeOnboarding(
        lastPeriodDate: LocalDate,
        averageCycleLength: Int,
        averagePeriodDuration: Int,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Insert initial baseline cycle
            val baselineCycle = CycleEntity(
                startDate = lastPeriodDate,
                periodDurationDays = averagePeriodDuration
            )
            cycleDao.insertCycle(baselineCycle)

            // 2. Populate initial active bleeding logs for the period
            for (day in 0 until averagePeriodDuration) {
                val bleedDate = lastPeriodDate.plusDays(day.toLong())
                dailyLogDao.upsertDailyLog(
                    DailyLogEntity(
                        date = bleedDate,
                        flow = if (day < 2) FlowIntensity.HEAVY else FlowIntensity.MEDIUM
                    )
                )
            }

            // 3. Mark completed
            prefs.setOnboardingCompleted(true)

            viewModelScope.launch(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
