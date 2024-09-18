package com.abielinski.wifiringer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.SharedPreferences
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var serviceSwitch: SwitchCompat
    private lateinit var startAtBootSwitch: SwitchCompat
    private lateinit var modifyMediaVolumeConnectSwitch: SwitchCompat
    private lateinit var modifyMediaVolumeDisconnectSwitch: SwitchCompat
    private lateinit var modifyVolumeDisconnectSlider: SeekBar
    private lateinit var modifyVolumeConnectSlider: SeekBar
    private lateinit var wifiManager: WifiManager
    private lateinit var statusText: TextView
    private lateinit var infoButton: Button

    private lateinit var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

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
        infoButton = findViewById(R.id.get_info_button)


        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        infoButton.setOnClickListener {

        }

        setupServiceSwitch()
        setupStartAtBootSwitch()
        setupActionSpinners()
        setupVolumeSwitches()
        setupVolumeSliders()
        setupPreferenceChangeListener()
        updateStatus()
    }

    private fun setupPreferenceChangeListener() {
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "isServiceRunning") {
                println("Preference $key updated")
                updateStatus()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("WifiPreferences", MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
    override fun onPause() {
        super.onPause()
        val sharedPreferences = getSharedPreferences("WifiPreferences", MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun setupServiceSwitch() {

        serviceSwitch.isChecked = getServicePref()
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            serviceSwitch.isChecked = false
        }else {
            val serviceIntent = Intent(this, WifiMonitorService::class.java)
            startForegroundService(serviceIntent)
            serviceSwitch.isChecked = true
            updateStatus()
        }
    }

    private fun stopWifiMonitorService() {
        val serviceIntent = Intent(this, WifiMonitorService::class.java)
        stopService(serviceIntent)
        updateStatus()
    }


    private fun updateStatus() {
        // This is a simple check. For a more robust solution, you might want to use ActivityManager

        val status = if (serviceSwitch.isChecked) {
            if (getServicePref()){
                "Service is running for all Wi-Fi networks"
            }else{
                "Setting is starting...."
            }
        } else {
            if (getServicePref()){
                "Service is stopping...."
            }else{
                "Service is not running"
            }
        }
        statusText.text = status
    }


    private fun getServicePref(): Boolean {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("isServiceRunning", false)
    }

    private fun infoButtonClicked() {

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) { // The requestCode you used in requestPermissions
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startWifiMonitorService()
            } else {
                // Permission denied
                Toast.makeText(this, "We need to show a persistent notification to operate in the background.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}