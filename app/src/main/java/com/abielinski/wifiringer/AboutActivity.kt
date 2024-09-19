package com.abielinski.wifiringer

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        var tvVersion: TextView = findViewById(R.id.tvVersion)
        tvVersion.text = """Version: $versionName"""

        findViewById<Button>(R.id.close_button).setOnClickListener {
            finish()
        }
    }
}