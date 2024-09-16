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
import android.os.IBinder
import androidx.core.app.NotificationCompat

class WifiMonitorService : Service() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var audioManager: AudioManager
    private lateinit var wifiManager: WifiManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
            val mode = getRingerModeFromPref(sharedPrefs.getInt("connectSpinner", 2))
            if (mode != 0) {
                setRingerMode(mode)
            }

            if (sharedPrefs.getBoolean("volumeOnConnect", false)){
                setMediaVolume(sharedPrefs.getInt("mediaVolumeOnConnect", 0))
            }
        }

        override fun onLost(network: Network) {
            val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
            val mode = getRingerModeFromPref(sharedPrefs.getInt("disconnectSpinner", 2))
            if (mode != 0) {
                setRingerMode(mode)
            }

            if (sharedPrefs.getBoolean("volumeOnDisconnect", false)){
                setMediaVolume(sharedPrefs.getInt("mediaVolumeOnDisconnect", 0))
            }
        }
    }

    private fun getRingerModeFromPref(pref: Int) :Int{
        if (pref == 0){
            return 0
        }
        if (pref == 1){
            return AudioManager.RINGER_MODE_VIBRATE
        }
        if (pref == 2){
            return AudioManager.RINGER_MODE_NORMAL
        }
        if (pref == 3){
            return AudioManager.RINGER_MODE_SILENT
        }
        return 0
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

        saveServiceState(true)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        saveServiceState(false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setRingerMode(mode: Int) {
        try {
            audioManager.ringerMode = mode
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun setMediaVolume(volume: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val desiredVolume = ((volume/100.0) * maxVolume).toInt()

        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0)
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Wi-Fi Monitor Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi Monitor")
            .setContentText("Monitoring Wi-Fi connection")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }


    private fun saveServiceState(isRunning: Boolean) {
        val sharedPrefs = getSharedPreferences("WifiPreferences", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("isServiceRunning", isRunning).apply()
    }

    companion object {
        private const val CHANNEL_ID = "WifiMonitorChannel"
        private const val NOTIFICATION_ID = 1
    }
}