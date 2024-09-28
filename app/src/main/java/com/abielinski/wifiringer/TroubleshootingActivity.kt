package com.abielinski.wifiringer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.provider.Settings
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

class TroubleshootingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_troubleshooting)

        findViewById<Button>(R.id.open_notifications).setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            this.startActivity(intent)
        }


        findViewById<Button>(R.id.open_permissions).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${packageName}")
            }
            this.startActivity(intent)
        }

        findViewById<Button>(R.id.more_info_button).setOnClickListener {
            val defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
            defaultBrowser.data = Uri.parse("https://dontkillmyapp.com/?app=WiFiRinger&0")
            startActivity(defaultBrowser)
        }

        findViewById<Button>(R.id.close_button).setOnClickListener {
            finish()
        }
    }
}