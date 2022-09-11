package com.zetwerk.filemanager.utils

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zetwerk.filemanager.R

internal object NotificationUtils {

    fun makeStatusNotification(
        title: String,
        message: String,
        context: Context,
        downloadId: Long?
    ) {

        // Make a channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                context.getString(R.string.app_name),
                importance
            )
            channel.description = description

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))
            .setAutoCancel(true)
        downloadId?.let {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            val pendingIntent =
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)

            builder.setContentIntent(pendingIntent)
        }
        NotificationManagerCompat.from(context).notify(Constants.NOTIFICATION_ID, builder.build())
    }
}