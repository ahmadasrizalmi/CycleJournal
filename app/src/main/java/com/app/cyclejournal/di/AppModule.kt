package com.app.cyclejournal.di

import android.content.Context
import com.app.cyclejournal.data.local.AppDatabase
import com.app.cyclejournal.data.local.dao.CycleDao
import com.app.cyclejournal.data.local.dao.DailyLogDao
import com.app.cyclejournal.data.preferences.OnboardingPreferences
import com.app.cyclejournal.data.remote.CloudflareBackupClient
import com.app.cyclejournal.domain.engine.ClinicalCycleEngine
import com.app.cyclejournal.domain.engine.CycleAggregator
import com.app.cyclejournal.domain.manager.DataRestoreManager
import com.app.cyclejournal.domain.manager.DataWipeManager
import com.app.cyclejournal.scheduler.alarm.CycleAlarmScheduler
import com.app.cyclejournal.security.BackupCryptoEngine
import com.app.cyclejournal.security.DatabaseKeyManager
import com.app.cyclejournal.security.SecurityPinManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("WorkerBaseUrl")
    fun provideWorkerBaseUrl(): String {
        return "https://backup-api.asridigital.com"
    }

    @Provides
    @Singleton
    fun provideSecurityPinManager(@ApplicationContext context: Context): SecurityPinManager {
        return SecurityPinManager(context)
    }

    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {
        return OnboardingPreferences(context)
    }

    @Provides
    @Singleton
    fun provideClinicalCycleEngine(): ClinicalCycleEngine {
        return ClinicalCycleEngine()
    }

    @Provides
    @Singleton
    fun provideCycleAggregator(
        dailyLogDao: DailyLogDao,
        cycleDao: CycleDao
    ): CycleAggregator {
        return CycleAggregator(dailyLogDao, cycleDao)
    }

    @Provides
    @Singleton
    fun provideBackupCryptoEngine(): BackupCryptoEngine {
        return BackupCryptoEngine()
    }

    @Provides
    @Singleton
    fun provideCloudflareBackupClient(@Named("WorkerBaseUrl") baseUrl: String): CloudflareBackupClient {
        return CloudflareBackupClient(baseUrl)
    }

    @Provides
    @Singleton
    fun provideDataRestoreManager(
        database: AppDatabase,
        cycleAggregator: CycleAggregator,
        backupClient: CloudflareBackupClient,
        cryptoEngine: BackupCryptoEngine
    ): DataRestoreManager {
        return DataRestoreManager(database, cycleAggregator, backupClient, cryptoEngine)
    }

    @Provides
    @Singleton
    fun provideCycleAlarmScheduler(@ApplicationContext context: Context): CycleAlarmScheduler {
        return CycleAlarmScheduler(context)
    }

    @Provides
    @Singleton
    fun provideDatabaseKeyManager(@ApplicationContext context: Context): DatabaseKeyManager {
        return DatabaseKeyManager(context)
    }

    @Provides
    @Singleton
    fun provideDataWipeManager(
        @ApplicationContext context: Context,
        database: AppDatabase,
        pinManager: SecurityPinManager,
        keyManager: DatabaseKeyManager,
        alarmScheduler: CycleAlarmScheduler,
        backupClient: CloudflareBackupClient
    ): DataWipeManager {
        return DataWipeManager(context, database, pinManager, keyManager, alarmScheduler, backupClient)
    }
}
