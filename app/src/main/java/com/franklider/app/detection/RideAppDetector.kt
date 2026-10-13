package com.franklider.app.detection

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.util.concurrent.TimeUnit

class RideAppDetector(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager
    private val knownPackages = listOf(
        "com.ubercab",
        "com.ubercab.driver",
        "com.yango.yango",
        "com.didiglobal.passenger",
        "com.rappi",
        "com.rappi.consumer",
        "com.taxibeat"
    )

    fun getInstalledRideApps(): List<String> {
        val installed = packageManager.getInstalledApplications(0)
        val found = mutableListOf<String>()
        for (info in installed) {
            if (knownPackages.contains(info.packageName)) {
                found.add(info.packageName)
            }
        }
        return found
    }

    fun getCurrentForegroundPackage(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.MINUTES.toMillis(10)
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.packageName
            }
        }

        return lastPackage
    }
}
