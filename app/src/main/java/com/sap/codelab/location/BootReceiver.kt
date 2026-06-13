package com.sap.codelab.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sap.codelab.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Repository.getOpen()
                    .filter { it.reminderLatitude != 0.0 || it.reminderLongitude != 0.0 }
                    .forEach { memo ->
                        GeofenceManager.registerGeofence(
                            appContext, memo.id, memo.reminderLatitude, memo.reminderLongitude
                        )
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}