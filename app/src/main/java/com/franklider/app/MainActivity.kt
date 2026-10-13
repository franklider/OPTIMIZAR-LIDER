package com.franklider.app

import android.Manifest
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.franklider.app.db.AppDatabase
import com.franklider.app.detection.RideAppDetector
import com.franklider.app.service.ForegroundLocationService
import com.franklider.app.utils.SettingsLauncher
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvDetectedApps: TextView
    private lateinit var btnOpenSettings: Button
    private lateinit var btnBattery: Button
    private lateinit var btnUsage: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnDelete: Button
    private lateinit var btnDetect: Button
    private lateinit var btnInfo: Button

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.all { it.value }
        tvStatus.text = if (allGranted) {
            "Permisos concedidos. Servicio listo."
        } else {
            "Faltan permisos. Activa ubicación y batería."
        }
        if (allGranted) startLocationService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.txtStatus)
        tvDetectedApps = findViewById(R.id.txtDetectedApps)
        btnOpenSettings = findViewById(R.id.btnOpenSettings)
        btnBattery = findViewById(R.id.btnBattery)
        btnUsage = findViewById(R.id.btnUsageAccess)
        btnStart = findViewById(R.id.btnStartService)
        btnStop = findViewById(R.id.btnStopService)
        btnDelete = findViewById(R.id.btnDeleteHistory)
        btnDetect = findViewById(R.id.btnDetectApps)
        btnInfo = findViewById(R.id.btnOpenAppInfo)

        btnOpenSettings.setOnClickListener { SettingsLauncher.openLocationSettings(this) }
        btnBattery.setOnClickListener { SettingsLauncher.openBatteryOptimization(this) }
        btnUsage.setOnClickListener { SettingsLauncher.openUsageAccess(this) }

        btnStart.setOnClickListener {
            if (hasLocationPermission()) {
                startLocationService()
            } else {
                requestLocationPermissions()
            }
        }

        btnStop.setOnClickListener { stopLocationService() }

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getInstance(this@MainActivity).locationDao().deleteAll()
                tvStatus.text = "Historial local borrado correctamente."
            }
        }

        btnDetect.setOnClickListener {
            val apps = RideAppDetector(this).getInstalledRideApps()
            tvDetectedApps.text = if (apps.isEmpty()) {
                "No se detectaron apps de rideshare."
            } else {
                "Apps detectadas:\n- " + apps.joinToString("\n- ")
            }
        }

        btnInfo.setOnClickListener {
            SettingsLauncher.openAppInfoForPackage(this, "com.ubercab")
            tvStatus.text = "Abre la info de la app objetivo para limpiar caché si lo deseas."
        }

        tvStatus.text = "Listo para optimizar ubicación."
        updateUiState()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val background = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return fine && coarse && background
    }

    private fun requestLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startLocationService() {
        val intent = ForegroundLocationService.newIntent(this)
        startService(intent)
        tvStatus.text = "Servicio de ubicación en segundo plano activo."
        updateUiState()
    }

    private fun stopLocationService() {
        val intent = ForegroundLocationService.newIntent(this)
        stopService(intent)
        tvStatus.text = "Servicio detenido."
        updateUiState()
    }

    private fun updateUiState() {
        val serviceRunning = isServiceRunning()
        btnStart.isEnabled = !serviceRunning
        btnStop.isEnabled = serviceRunning
    }

    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(ActivityManager::class.java)
        return manager?.getRunningServices(Integer.MAX_VALUE)?.any {
            it.service.className == ForegroundLocationService::class.java.name
        } == true
    }
}
