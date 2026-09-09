package com.app.cyclejournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.cyclejournal.data.local.dao.CycleDao
import com.app.cyclejournal.data.local.dao.DailyLogDao
import com.app.cyclejournal.data.local.entity.CycleEntity
import com.app.cyclejournal.data.local.entity.DailyLogEntity
import com.app.cyclejournal.security.DatabaseKeyManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.util.Arrays

@Database(
    entities = [DailyLogEntity::class, CycleEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyLogDao(): DailyLogDao
    abstract fun cycleDao(): CycleDao

    companion object {
        const val DATABASE_NAME = "cycle_journal_secure.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns singleton AppDatabase instance encrypted with SQLCipher.
         * Derives passphrase via DatabaseKeyManager and wipes raw byte array from RAM.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            // 1. Initialize Zetetic SQLCipher native JNI binaries
            System.loadLibrary("sqlcipher")

            // 2. Retrieve hardware-protected database passphrase
            val keyManager = DatabaseKeyManager(appContext)
            val passphrase = keyManager.getOrCreateDatabasePassphrase()

            // 3. Construct SupportFactory
            val factory = SupportOpenHelperFactory(passphrase)

            // 4. Memory hygiene: Wipe raw plaintext passphrase array from RAM
            Arrays.fill(passphrase, 0.toByte())

            // 5. Build encrypted Room instance
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * Closes database and resets instance for Nuke / Data Wipe operations.
         */
        @Synchronized
        fun closeAndResetInstance() {
            INSTANCE?.let { db ->
                if (db.isOpen) {
                    db.close()
                }
            }
            INSTANCE = null
        }
    }
}
