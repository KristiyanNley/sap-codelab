package com.sap.codelab.repository

import android.app.Application
import com.sap.codelab.notification.NotificationHelper
import org.osmdroid.config.Configuration
import java.io.File

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
        NotificationHelper.createNotificationChannel(this)
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidTileCache = File(cacheDir, "osmdroid")
        }
    }
}