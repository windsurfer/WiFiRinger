package com.abielinski.wifiringer
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var serviceSwitch: Switch
    private lateinit var wifiListRecyclerView: RecyclerView
    private lateinit var wifiListAdapter: WifiListAdapter
    private lateinit var wifiManager: WifiManager
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceSwitch = findViewById(R.id.serviceSwitch)
        wifiListRecyclerView = findViewById(R.id.wifiListRecyclerView)
        statusText = findViewById(R.id.statusText)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        setupServiceSwitch()
        setupWifiList()
        updateStatus()

        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            refreshWifiList()
        }
    }

    private fun setupServiceSwitch() {
        serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startWifiMonitorService()
            } else {
                stopWifiMonitorService()
            }
            updateStatus()
        }
    }

    private fun setupWifiList() {
        wifiListAdapter = WifiListAdapter(mutableListOf()) { _, _ ->
            updateStatus()
        }
        wifiListRecyclerView.adapter = wifiListAdapter
        wifiListRecyclerView.layoutManager = LinearLayoutManager(this)
        refreshWifiList()
    }

    private fun refreshWifiList() {
        val wifiList = wifiManager.configuredNetworks.map { it.SSID.removeSurrounding("\"") }
        wifiListAdapter.updateList(wifiList)
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
        val selectedNetworks = wifiListAdapter.getSelectedNetworks()
        val status = if (serviceSwitch.isChecked) {
            if (selectedNetworks.isEmpty()) {
                "Service is running for all Wi-Fi networks"
            } else {
                "Service is running for selected Wi-Fi networks: ${selectedNetworks.joinToString(", ")}"
            }
        } else {
            "Service is not running"
        }
        statusText.text = status
    }
}