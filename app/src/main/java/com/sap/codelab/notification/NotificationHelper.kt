package com.sap.codelab.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sap.codelab.R
import com.sap.codelab.model.Memo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo

private const val CHANNEL_ID = "memo_location_channel"
private const val CHANNEL_NAME = "Location Reminders"
private const val NOTIFICATION_TEXT_LIMIT = 140

internal object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Triggered when you arrive at a memo location"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMemoNotification(context: Context, memo: Memo) {
        val contentText = memo.description.take(NOTIFICATION_TEXT_LIMIT)

        val tapIntent = Intent(context, ViewMemo::class.java).apply {
            putExtra(BUNDLE_MEMO_ID, memo.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            memo.id.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location_pin)
            .setContentTitle(memo.title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
    }
}