package com.zetwerk.filemanager

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.zetwerk.filemanager.utils.Constants.READ_EXTERNAL_STORAGE_PERMISSION
import com.zetwerk.filemanager.utils.FileManager.isPdf
import com.zetwerk.filemanager.databinding.ActivityMainBinding
import com.zetwerk.filemanager.utils.Constants
import com.zetwerk.filemanager.utils.DownloadManager
import com.zetwerk.filemanager.utils.FileManager

class MainActivity : AppCompatActivity() {
    lateinit var dataBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        clickListeners()
    }

    private fun clickListeners() {
        dataBinding.openFileBt.setOnClickListener {
            FileManager.openFileForResult(fileResultLauncher)
        }

        dataBinding.openFolderBt.setOnClickListener {
            FileManager.openFolderForResult(folderResultLauncher)
        }

        dataBinding.downloadImageDownloadBt.setOnClickListener {
            downloadImage()
        }
        dataBinding.downloadImageAppFolderBt.setOnClickListener {
            downloadImageToAppFolder()
        }
        dataBinding.clearCache.setOnClickListener {
            FileManager.clearCache(this)
        }
        dataBinding.launchFilePreview.setOnClickListener {
            startActivity(Intent(this, FilePreviewActivity::class.java))
        }
    }

    private fun downloadImage() {
        val fileMap = HashMap<String, String>()
        fileMap["sample"] = Constants.downloadUrl
        fileMap["sample2"] = Constants.downloadUrl
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (permissionToReadWrite) {
                DownloadManager.downloadImageToDownloadFolder(this, fileMap)
            } else {
                permissionForReadWrite()
            }
        } else {
            DownloadManager.downloadImageToDownloadFolder(this, fileMap)
        }
    }

    private fun downloadImageToAppFolder() {
        val bitmap = (ContextCompat.getDrawable(this, R.drawable.test) as BitmapDrawable).bitmap

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (permissionToReadWrite) {
                DownloadManager.downloadToAppFolder(this, bitmap)
            } else {
                permissionForReadWrite()
            }

        } else {
            DownloadManager.downloadToAppFolder(this, bitmap)
        }
    }

    private var fileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val fileMap = FileManager.handleOpenMultipleFileResult(result, this.contentResolver)
            val files = FileManager.prepareUrisForUpload(this, fileMap)

            for (file in files) {
                Log.e("REAL PATH", file.body.contentType().toString())
                Log.e("REAL PATH", file.body.contentLength().toString())
                Log.e("REAL PATH", file.body.toString())
            }

            fileMap?.let {
                val fileItem = it.entries.firstOrNull()
                if (fileItem != null) {
                    if (!fileItem.key.isPdf(this))
                        dataBinding.image.setImageBitmap(
                            FileManager.getBitmapFromUri(
                                this,
                                fileItem.value
                            )
                        )
                    else
                        FileManager.renderPdfFromUri(fileItem.value, this, dataBinding.image)
                }
            }
        }

    private var folderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            FileManager.handleOpenFolderResult(result, this, application)
        }


    private var permissionToReadWrite: Boolean = false
        get() {
            val permissionGrantedResult: Int = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            return permissionGrantedResult == PackageManager.PERMISSION_GRANTED
        }

    //Request Permission For Read Storage
    private fun permissionForReadWrite() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), READ_EXTERNAL_STORAGE_PERMISSION
        )
    }

}
