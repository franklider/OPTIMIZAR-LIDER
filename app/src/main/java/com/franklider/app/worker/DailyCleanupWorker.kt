package com.franklider.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.franklider.app.db.AppDatabase

class DailyCleanupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            db.locationDao().deleteAll()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
