package com.app.cyclejournal.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cyclejournal.data.local.dao.CycleDao
import com.app.cyclejournal.data.local.dao.DailyLogDao
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.data.local.entity.FlowIntensity
import com.app.cyclejournal.domain.engine.ClinicalCycleEngine
import com.app.cyclejournal.domain.engine.CycleAggregator
import com.app.cyclejournal.domain.model.AnomalyAlert
import com.app.cyclejournal.domain.model.CycleStats
import com.app.cyclejournal.domain.model.FertilePrediction
import com.app.cyclejournal.export.pdf.ClinicalPdfReportGenerator
import com.app.cyclejournal.export.pdf.PdfShareHelper
import com.app.cyclejournal.scheduler.alarm.CycleAlarmScheduler
import com.app.cyclejournal.security.SecurityPinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class CycleViewModel @Inject constructor(
    private val dailyLogDao: DailyLogDao,
    private val cycleDao: CycleDao,
    private val cycleAggregator: CycleAggregator,
    private val clinicalEngine: ClinicalCycleEngine,
    private val alarmScheduler: CycleAlarmScheduler,
    private val pinManager: SecurityPinManager
) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            cycleAggregator.reconcileAllHistory()
        }
    }

    // 1. Reactive flow of all cycles
    val allCyclesFlow: StateFlow<List<CycleEntity>> = cycleDao.getAllCyclesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 1b. All daily logs (biomarkers, pain, notes) — for week strip, BBT chart, recent history
    val allLogsFlow: StateFlow<List<DailyLogEntity>> = dailyLogDao.getAllLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Latest active cycle
    val latestCycleFlow: StateFlow<CycleEntity?> = allCyclesFlow.map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 3. Completed cycles for FIGO calculation
    val completedCyclesFlow: StateFlow<List<CycleEntity>> = allCyclesFlow.map {
        it.filter { cycle -> cycle.cycleLengthDays != null }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. FIGO Cycle Stats
    val cycleStatsFlow: StateFlow<CycleStats?> = completedCyclesFlow.map {
        clinicalEngine.calculateCycleStats(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 5. Fertile Window & Ovulation Prediction
    val fertilePredictionFlow: StateFlow<FertilePrediction?> = combine(
        latestCycleFlow,
        cycleStatsFlow
    ) { latest, stats ->
        if (latest != null) {
            val avgLen = stats?.averageLength ?: 28.0
            val prediction = clinicalEngine.predictFertileWindow(latest.startDate, avgLen)
            // Reschedule H-2 period alert
            alarmScheduler.schedulePeriodAlert(prediction.predictedNextPeriodDate)
            prediction
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 6. Active Bleeding Dates in current records (Flow: LIGHT, MEDIUM, HEAVY)
    val periodDatesFlow: StateFlow<Set<LocalDate>> = dailyLogDao.getAllLogsFlow().map { logs ->
        logs.filter { it.flow in listOf(FlowIntensity.LIGHT, FlowIntensity.MEDIUM, FlowIntensity.HEAVY) }
            .map { it.date }
            .toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // 7. Clinical Anomalies & Red Flags
    val anomaliesFlow: StateFlow<List<AnomalyAlert>> = combine(
        latestCycleFlow,
        completedCyclesFlow,
        dailyLogDao.getAllLogsFlow()
    ) { latest, completed, logs ->
        clinicalEngine.evaluateAnomalies(latest, completed, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getLogForDate(date: LocalDate): Flow<DailyLogEntity?> = flow {
        emit(dailyLogDao.getLogByDate(date))
    }

    fun saveDailyLog(log: DailyLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            cycleAggregator.onDailyLogSaved(log)
        }
    }

    fun exportAndSharePdfReport(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val patientId = pinManager.getOrCreateAnonymousUserId()
            val stats = cycleStatsFlow.value
            val cycles = completedCyclesFlow.value
            val anomalies = anomaliesFlow.value

            val cacheDir = File(context.cacheDir, "reports")
            cacheDir.mkdirs()
            val pdfFile = File(cacheDir, "CycleJournal_${patientId}_${System.currentTimeMillis()}.pdf")

            val generator = ClinicalPdfReportGenerator(context)
            generator.generateReport(
                outputFile = pdfFile,
                patientIdentifier = patientId,
                stats = stats,
                cycles = cycles,
                anomalies = anomalies
            )

            viewModelScope.launch(Dispatchers.Main) {
                PdfShareHelper.sharePdf(context, pdfFile)
            }
        }
    }

    fun getCurrentCycleDay(startDate: LocalDate?): Long {
        if (startDate == null) return 1L
        return ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1L
    }
}
