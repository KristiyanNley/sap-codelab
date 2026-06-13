package com.sap.codelab.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.notification.NotificationHelper
import com.sap.codelab.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError() || event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                event.triggeringGeofences?.forEach { geofence ->
                    val memoId = geofence.requestId.toLongOrNull() ?: return@forEach
                    val memo = Repository.getMemoById(memoId)
                    NotificationHelper.showMemoNotification(appContext, memo)
                    GeofenceManager.removeGeofence(appContext, geofence.requestId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}