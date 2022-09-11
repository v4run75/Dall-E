package com.zetwerk.filemanager.utils

import android.app.Activity
import android.app.Application
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.documentfile.provider.DocumentFile
import com.zetwerk.filemanager.ContentUriRequestBody
import com.zetwerk.filemanager.ImagePreviewActivity
import com.zetwerk.filemanager.R
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min


object FileManager {

    fun openFileForResult(
        fileResultLauncher: ActivityResultLauncher<Intent>,
        allowMultiple: Boolean? = true,
        imageOnly: Boolean? = false
    ) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = if (imageOnly == true) {
                "image/*"
            } else {
                "*/*"
            }
            addCategory(Intent.CATEGORY_OPENABLE)
            flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        fileResultLauncher.launch(intent)
    }

    fun openFolderForResult(folderResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        folderResultLauncher.launch(intent)
    }

    //Call this method inside registerForActivityResult
    fun handleOpenFolderResult(result: ActivityResult, context: Context, application: Application) {
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data = result.data
            val directoryUri = data?.data ?: return
            //Taking permission to retain access
            context.contentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            //Now you have access to the folder, you can easily view the content or do whatever you want.
            val documentsTree = DocumentFile.fromTreeUri(application, directoryUri) ?: return
            val childDocuments = documentsTree.listFiles().asList()
            Toast.makeText(
                context,
                "Total Items Under this folder =" + childDocuments.size.toString(),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Call this method inside registerForActivityResult
    fun handleOpenMultipleFileResult(result: ActivityResult, contentResolver: ContentResolver): HashMap<String, Uri>? {
        if (result.resultCode == Activity.RESULT_OK) {
            val uris = result.data?.let { getSelectedUris(it) }

            val fileMap = HashMap<String, Uri>()

            uris?.forEachIndexed { index, uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                fileMap[queryName(contentResolver,uri).toString()] = uri
            }
            return fileMap
        }
        return null
    }

    fun prepareUrisForUpload(context: Context, fileMap: HashMap<String, Uri>?): MutableList<MultipartBody.Part> {
        val files: MutableList<MultipartBody.Part> = ArrayList()
        fileMap?.forEach { (name, uri) ->
            prepareFilePart(context, uri, name)?.let {
                files.add(it)
            }
        }
        return files
    }

    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")

        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return bitmap
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun prepareFilePart(context: Context, uri:Uri, name: String):MultipartBody.Part?{
        var body:MultipartBody.Part? = null
        var requestFile: RequestBody

        requestFile = ContentUriRequestBody(context.contentResolver, uri)
        Log.e("NAME", queryName(context.contentResolver, uri).toString())
        body = MultipartBody.Part.createFormData("files", name, requestFile)
        return body
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String? {
        val returnCursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    private fun getSelectedUris(resultIntent: Intent): List<Uri>? =
        resultIntent.clipData
            ?.takeIf { it.itemCount != 0 }
            ?.let { clipDataItem ->
                (0 until clipDataItem.itemCount)
                    .map { clipDataItem.getItemAt(it).uri }
            }
            ?: resultIntent.data?.let { listOf(it) }

/*
    //Call this method inside registerForActivityResult
    fun handleOpenFileResult(result: ActivityResult, context: Context, image: ImageView) {
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data = result.data
            data?.data?.also { documentUri ->
                //Permission needed if you want to retain access even after reboot
                context.contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                Toast.makeText(context, documentUri.path.toString(), Toast.LENGTH_LONG).show()
//                Log.e("REAL PATH", getFilePathFromMediaStore(context,documentUri).toString())
                Log.e("REAL PATH", ContentUriRequestBody(context.contentResolver,documentUri).contentType().toString())
                Log.e("REAL PATH", ContentUriRequestBody(context.contentResolver,documentUri).contentLength().toString())
                Log.e("REAL PATH", ContentUriRequestBody(context.contentResolver,documentUri).toString())

                val parcelFileDescriptor =
                    context.contentResolver.openFileDescriptor(documentUri, "r")

                if (ContentUriRequestBody(context.contentResolver, documentUri).contentType().toString().equals("application/pdf", false)) {
                    renderPdf(parcelFileDescriptor, context, image)
                } else {
                    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                    image.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor))
                    parcelFileDescriptor?.close()
                }


            }
        }
    }
*/

    fun renderPdfFromUri(
        uri: Uri,
        context: Context,
        image: ImageView,
    ) {
        val parcelFileDescriptor =
            context.contentResolver.openFileDescriptor(uri, "r")

        parcelFileDescriptor?.let {
            val pdfRenderer = PdfRenderer(it)
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels

            val page = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(
                screenWidth,
                (screenWidth.toFloat() / page.width * page.height).toInt(),
                Bitmap.Config.ARGB_8888
            )
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // do something with the bitmap, like putting it on an ImageView
            image.setImageBitmap(bitmap)
            page.close()
            pdfRenderer.close()
        }
        parcelFileDescriptor?.close()
    }

    fun clearCache(context: Context) {
        context.cacheDir.deleteRecursively()
    }

   private fun getFilePathFromMediaStore(context: Context, contentUri: Uri): String? {
        try {
            val filePathColumn = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
            )

            val returnCursor = contentUri.let { context.contentResolver.query(it, filePathColumn, null, null, null) }

            if (returnCursor != null) {

                returnCursor.moveToFirst()
                val nameIndex = returnCursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                val name = returnCursor.getString(nameIndex)
                val file = File(context.cacheDir, name)
                val inputStream = context.contentResolver.openInputStream(contentUri)
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                val bufferSize = min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)

                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }

                inputStream.close()
                outputStream.close()
                return file.absolutePath
            }
            else
            {
                Log.d("","returnCursor is null")
                return null
            }
        }
        catch (e: Exception) {
            Log.d("","exception caught at getFilePath(): $e")
            return null
        }
    }

    fun String.isPdf(context: Context): Boolean {
        val extension: String = getExtension()
        val mimeType = getMimeType(context)
        return extension.containsIgnoreCase(Constants.PDF) || mimeType?.containsIgnoreCase(Constants.MIME_PDF)== true
    }

    fun String.getExtension(): String {
        var extension = ""
        val extensionStartIndex = this.lastIndexOf(".")
        if (extensionStartIndex > -1 && extensionStartIndex < this.length) {
            extension = this.substring(extensionStartIndex)
        }
        return extension
    }

    fun String.getMimeType(context: Context): String? {
        return context.getMimeType(Uri.parse(this))
    }

    fun String.containsIgnoreCase(compare: String): Boolean {
        var original = this.toLowerCase()
        var compareLowerCase = compare.toLowerCase()
        return original.contains(compareLowerCase)

    }

    fun Context.getMimeType(uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr: ContentResolver = this.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase()
            )
        }
        return mimeType
    }

    fun openFile(uri: Uri?, url: String?, activity: Activity, startingView: View? = null) {
        uri?.let {
            try {
                url?.let { url ->
                    if (url.containsIgnoreCase(".jpg") || url.containsIgnoreCase(".jpeg") || url.containsIgnoreCase(
                            ".png"
                        ) || url.containsIgnoreCase(".JPG") || url.containsIgnoreCase(".PNG")
                    ) {
                        val intent = Intent(activity, ImagePreviewActivity::class.java)
                        intent.putExtra("file_uri", uri.toString())
                        //Image zoom animation
                        val transitionName = activity.getString(R.string.transistion_image)
                        var options : Bundle? = null
                        startingView?.let {
                            options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                activity,
                                startingView,
                                transitionName
                            ).toBundle()
                        }
                        activity.startActivity(intent, options)
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val extension = url.getExtension().replace(".", "")
                        val mimeType = extension.getMimeTypeFromExtension()

                        intent.setDataAndType(uri, mimeType)
                        activity.startActivity(intent)
                    }
                }
            } catch (ae: ActivityNotFoundException) {
                Toast.makeText(activity, "No application found to open this document", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(Constants.TAG_EXCEPTIONS, e.message ?: "")
            }
        }
    }


    fun String.getMimeTypeFromExtension(): String {
        return when (this) {
            "pdf" -> "application/pdf"
            "rtf" -> "application/rtf"
            "wav", "mp3" -> "audio/*"
            "gif" -> "image/gif"
            "jpg", "jpeg", "png", "JPG", "PNG" -> "image/jpeg"
            "txt" -> "text/plain"
            "3gp", "mpg", "mpeg", "mpe", "mp4", "avi" -> "video/*"
            "doc" -> "application/msword"
            "dot" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "dotx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.template"
            "docm" -> "application/vnd.ms-word.document.macroEnabled.12"
            "dotm" -> "application/vnd.ms-word.template.macroEnabled.12"
            "xls", "xlt", "xla"  -> Constants.FILE_TYPE_EXCEL
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xltx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.template"
            "xlsm" -> "application/vnd.ms-excel.sheet.macroEnabled.12"
            "xltm" -> "application/vnd.ms-excel.template.macroEnabled.12"
            "xlam" -> "application/vnd.ms-excel.addin.macroEnabled.12"
            "xlsb" -> "application/vnd.ms-excel.sheet.binary.macroEnabled.12"
            "ppt", "pot", "pps", "ppa" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "potx" -> "application/vnd.openxmlformats-officedocument.presentationml.template"
            "ppsx" -> "application/vnd.openxmlformats-officedocument.presentationml.slideshow"
            "ppam" -> "application/vnd.ms-powerpoint.addin.macroEnabled.12"
            "pptm" -> "application/vnd.ms-powerpoint.presentation.macroEnabled.12"
            "potm" -> "application/vnd.ms-powerpoint.template.macroEnabled.12"
            "ppsm" -> "application/vnd.ms-powerpoint.slideshow.macroEnabled.12"
            "mdb" -> "application/vnd.ms-access"
            else -> "*/*"
        }
    }


}