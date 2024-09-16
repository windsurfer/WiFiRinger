package com.abielinski.wifiringer
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private lateinit var serviceSwitch: SwitchCompat
    private lateinit var startAtBootSwitch: SwitchCompat
    private lateinit var modifyMediaVolumeConnectSwitch: SwitchCompat
    private lateinit var modifyMediaVolumeDisconnectSwitch: SwitchCompat
    private lateinit var modifyVolumeDisconnectSlider: SeekBar
    private lateinit var modifyVolumeConnectSlider: SeekBar
    private lateinit var wifiManager: WifiManager
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceSwitch = findViewById(R.id.serviceSwitch)
        startAtBootSwitch = findViewById(R.id.startAtBootSwitch)
        modifyMediaVolumeConnectSwitch = findViewById(R.id.modifyMediaVolumeConnectSwitch)
        modifyMediaVolumeDisconnectSwitch = findViewById(R.id.modifyMediaVolumeDisconnectSwitch)
        modifyVolumeDisconnectSlider = findViewById(R.id.modifyVolumeDisconnectSeekbar)
        modifyVolumeConnectSlider = findViewById(R.id.modifyVolumeConnectSeekbar)
        statusText = findViewById(R.id.statusText)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        setupServiceSwitch()
        setupStartAtBootSwitch()
        setupActionSpinners()
        setupVolumeSwitches()
        setupVolumeSliders()
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
    private fun setupActionSpinners() {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)

        val disconnectSpinner: Spinner = findViewById(R.id.disconnectSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.action_states_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            disconnectSpinner.adapter = adapter
        }
        disconnectSpinner.setSelection(sharedPrefs.getInt("disconnectSpinner", 1))
        disconnectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                //val selectedItem = parent.getItemAtPosition(position).toString()
                sharedPrefs.edit().putInt("disconnectSpinner", position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                sharedPrefs.edit().putInt("disconnectSpinner", 1).apply()
            }
        }

        val connectSpinner: Spinner = findViewById(R.id.connectSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.action_states_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            connectSpinner.adapter = adapter
        }
        connectSpinner.setSelection(sharedPrefs.getInt("connectSpinner", 2))


        connectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                //val selectedItem = parent.getItemAtPosition(position).toString()
                sharedPrefs.edit().putInt("connectSpinner", position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                sharedPrefs.edit().putInt("connectSpinner", 2).apply()
            }
        }
    }


    private fun setupStartAtBootSwitch() {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        startAtBootSwitch.isChecked = sharedPrefs.getBoolean("startAtBoot", false)
        startAtBootSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("startAtBoot", isChecked).apply()
        }
    }

    private fun setupVolumeSwitches() {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        modifyMediaVolumeConnectSwitch.isChecked = sharedPrefs.getBoolean("volumeOnConnect", false)
        modifyMediaVolumeConnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("volumeOnConnect", isChecked).apply()
        }

        modifyMediaVolumeDisconnectSwitch.isChecked = sharedPrefs.getBoolean("volumeOnDisconnect", false)
        modifyMediaVolumeDisconnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("volumeOnDisconnect", isChecked).apply()
        }
    }

    private fun setupVolumeSliders() {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        modifyVolumeConnectSlider.progress = sharedPrefs.getInt("mediaVolumeOnConnect", 0)
        modifyVolumeConnectSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPrefs.edit().putInt("mediaVolumeOnConnect", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        modifyVolumeDisconnectSlider.progress = sharedPrefs.getInt("mediaVolumeOnDisconnect", 0)
        modifyVolumeDisconnectSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPrefs.edit().putInt("mediaVolumeOnDisconnect", progress).apply()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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