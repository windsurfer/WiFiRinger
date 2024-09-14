package com.abielinski.wifiringer

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class WifiMonitorService : Service() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var audioManager: AudioManager
    private lateinit var wifiManager: WifiManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val currentSSID = getCurrentSSID()
            if (shouldChangeRingerMode(currentSSID)) {
                setRingerMode(AudioManager.RINGER_MODE_NORMAL)
            }
        }

        override fun onLost(network: Network) {
            setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
        }
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setRingerMode(mode: Int) {
        audioManager.ringerMode = mode
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wi-Fi Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi Monitor")
            .setContentText("Monitoring Wi-Fi connection")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    private fun getCurrentSSID(): String? {
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo?.ssid?.removeSurrounding("\"")
    }

    private fun shouldChangeRingerMode(ssid: String?): Boolean {
        val selectedNetworks = getSelectedNetworks()
        return selectedNetworks.isEmpty() || selectedNetworks.contains(ssid)
    }

    private fun getSelectedNetworks(): Set<String> {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet("selectedNetworks", emptySet()) ?: emptySet()
    }

    companion object {
        private const val CHANNEL_ID = "WifiMonitorChannel"
        private const val NOTIFICATION_ID = 1
    }
}