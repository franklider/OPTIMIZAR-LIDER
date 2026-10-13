package com.franklider.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.franklider.app.worker.DailyCleanupWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val request = PeriodicWorkRequestBuilder<DailyCleanupWorker>(24, TimeUnit.HOURS)
            .addTag("daily_cleanup")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
