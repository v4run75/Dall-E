package com.zetwerk.filemanager.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.zetwerk.filemanager.R
import java.io.File
import java.io.FileOutputStream

object DownloadManager {
    fun downloadImageToDownloadFolder(context: Context, filesMap: HashMap<String, String>?) {

        if (filesMap == null || filesMap.size == 0) {

            val errorMessage = context.getString(R.string.download_error_default_message)
            Toast.makeText(context, "Download Error: $errorMessage", Toast.LENGTH_LONG).show()

            return
        }

        val mgr = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadReceiver = getDownloadReceiver()

        filesMap.forEach { (fileName, fileUrl) ->
            val downloadUri = Uri.parse(fileUrl)
            val request = DownloadManager.Request(
                downloadUri
            )
            request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
                .setAllowedOverRoaming(false).setTitle(fileName)
                .setDescription("Downloading file(s)")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
                )

            NotificationUtils.makeStatusNotification(
                "Downloading file(s)...",
                fileName,
                context,
                null
            )
            mgr.enqueue(request)
        }
        context.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    private fun getDownloadReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(mContext: Context?, intent: Intent?) {
                val action = intent?.action
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    mContext?.let { context ->
                        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                        NotificationUtils.makeStatusNotification(
                            "Downloaded file(s)",
                            "Click to view the file(s)",
                            context,
                            downloadId
                        )
                        Log.e("DownloadReceiver---->", "DownloadCompleted")
                    }
                }
            }
        }
    }

    //Downloading file to Internal Folder
    fun downloadToAppFolder(context: Context, bitmap: Bitmap, fileName: String = "default.png") {
        try {
            val file = File(
                context.getExternalFilesDir(
                    null
                ), fileName
            )

            if (!file.exists())
                file.createNewFile()

            var fileOutputStream: FileOutputStream? = null

            fileOutputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream)
            Toast.makeText(
                context,
                context.getString(R.string.download_successful) + file.absolutePath,
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}