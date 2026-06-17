package com.sap.codelab.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

internal const val GEOFENCE_RADIUS_METERS = 200f
private const val TAG = "GeofenceManager"

internal object GeofenceManager {

    @SuppressLint("MissingPermission")
    fun registerGeofence(context: Context, memoId: Long, latitude: Double, longitude: Double) {
        val geofence = Geofence.Builder()
            .setRequestId(memoId.toString())
            .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        LocationServices.getGeofencingClient(context)
            .addGeofences(request, buildPendingIntent(context))
            .addOnFailureListener { e ->
                val errorMessage = GeofenceStatusCodes.getStatusCodeString(
                    (e as? com.google.android.gms.common.api.ApiException)?.statusCode ?: -1
                )
                Log.e(TAG, "Geofence registration failed for memo $memoId: $errorMessage", e)
            }
    }

    fun removeGeofence(context: Context, memoId: String) {
        LocationServices.getGeofencingClient(context)
            .removeGeofences(listOf(memoId))
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}