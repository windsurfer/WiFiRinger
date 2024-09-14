package com.abielinski.wifiringer
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private lateinit var serviceSwitch: SwitchCompat
    private lateinit var startAtBootSwitch: SwitchCompat
    private lateinit var wifiManager: WifiManager
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceSwitch = findViewById(R.id.serviceSwitch)
        startAtBootSwitch = findViewById(R.id.startAtBootSwitch)
        statusText = findViewById(R.id.statusText)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        setupServiceSwitch()
        setupStartAtBootSwitch()
        updateStatus()
    }

    private fun setupServiceSwitch() {

        serviceSwitch.isChecked = isServiceRunning()
        serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startWifiMonitorService()
            } else {
                stopWifiMonitorService()
            }
            updateStatus()
        }
    }


    private fun setupStartAtBootSwitch() {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        startAtBootSwitch.isChecked = sharedPrefs.getBoolean("startAtBoot", false)
        startAtBootSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("startAtBoot", isChecked).apply()
        }
    }

    private fun startWifiMonitorService() {
        val serviceIntent = Intent(this, WifiMonitorService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun stopWifiMonitorService() {
        val serviceIntent = Intent(this, WifiMonitorService::class.java)
        stopService(serviceIntent)
    }

    private fun updateStatus() {
        val status = if (serviceSwitch.isChecked) {
            "Service is running for all Wi-Fi networks"
        } else {
            "Service is not running"
        }
        statusText.text = status
    }


    private fun isServiceRunning(): Boolean {
        // This is a simple check. For a more robust solution, you might want to use ActivityManager
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("isServiceRunning", false)
    }
}