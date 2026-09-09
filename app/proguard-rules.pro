# ==============================================================================
# 1. SQLCIPHER FOR ANDROID (ZETETIC) & JNI RULES
# ==============================================================================
-keep class net.zetetic.** { *; }
-dontwarn net.zetetic.**
-keep class androidx.sqlite.db.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# ==============================================================================
# 2. ANDROIDX ROOM COMPILER & DATABASE RUNTIME
# ==============================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ==============================================================================
# 3. ENTITIES, DOMAIN MODELS & ENUMS (SERIALIZATION INTEGRITY)
# ==============================================================================
-keep class com.app.cyclejournal.data.local.entity.** { *; }
-keepclassmembers enum com.app.cyclejournal.data.local.entity.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}
-keep class com.app.cyclejournal.domain.model.** { *; }
-keep class com.app.cyclejournal.data.backup.** { *; }

-dontwarn com.google.errorprone.annotations.**
# ==============================================================================
# 4. KRIPTOGRAFI, KEYSTORE, & WORKMANAGER
# ==============================================================================
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.app.cyclejournal.security.** { *; }

-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

-keep class com.app.cyclejournal.scheduler.alarm.CycleNotificationReceiver { *; }
-keep class com.app.cyclejournal.scheduler.alarm.BootReceiver { *; }

# ==============================================================================
# 5. KOTLIN COROUTINES & JETPACK COMPOSE RUNTIME
# ==============================================================================
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-dontwarn kotlinx.coroutines.**
-keep class androidx.compose.runtime.** { *; }
