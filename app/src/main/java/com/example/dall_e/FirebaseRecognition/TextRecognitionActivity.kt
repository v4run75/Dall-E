package com.example.dall_e.FirebaseRecognition

import android.Manifest
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dall_e.BuildConfig
import com.example.dall_e.R
import com.example.dall_e.databinding.ActivityTextRecogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.hitanshudhawan.firebasemlkitexample.textrecognition.TextRecognitionAdapter
import com.hitanshudhawan.firebasemlkitexample.textrecognition.TextRecognitionModel
import java.io.File

class TextRecognitionActivity : AppCompatActivity() {

    private val imageView by lazy { findViewById<ImageView>(R.id.text_recognition_image_view)!! }

    private val bottomSheetButton by lazy { findViewById<FrameLayout>(R.id.bottom_sheet_button)!! }
    private val bottomSheetRecyclerView by lazy { findViewById<RecyclerView>(R.id.bottom_sheet_recycler_view)!! }
    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(findViewById(R.id.bottom_sheet)!!) }

    private var context: Context? = null
    private val textRecognitionModels = ArrayList<TextRecognitionModel>()
    private var nameString = ""
    private var emailString = ""
    private var altEmailString = ""
    private var phoneString = ""
    private var customphoneString = ""
    private var altPhoneString = ""
    private var websiteString = ""
    private var cardSet = true
    private var isNameSet = false
    private var isPhoneSet = false
    private var isEmailSet = false

    private lateinit var binding: ActivityTextRecogBinding


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { flag ->
        // use bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, latestTmpUri!!)
            val bitmap = ImageDecoder.decodeBitmap(source)
            analyzeImage(bitmap)
        } else {
            analyzeImage(MediaStore.Images.Media.getBitmap(contentResolver, latestTmpUri))
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    takeImage() // access to the camera is allowed, open the camera
                }
                !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    // access to the camera is denied, the user has checked the Don't ask again.
                }
                else -> {
                    // access to the camera is prohibited
                }
            }
        }

    private var latestTmpUri: Uri? = null

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTextRecogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Scan Card"



        bottomSheetButton.setOnClickListener {
            isNameSet = false
            isPhoneSet = false
            isEmailSet = false
            nameString = ""
            emailString = ""
            altEmailString = ""
            phoneString = ""
            altPhoneString = ""
            websiteString = ""

            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // explain to the user why the permission is needed
            } else {
                permission.launch(Manifest.permission.CAMERA)
            }


//            FileManager.openFileForResult(fileResultLauncher = activityLauncher,false, true)
        }


        bottomSheetRecyclerView.layoutManager = LinearLayoutManager(this)
        bottomSheetRecyclerView.adapter = TextRecognitionAdapter(this, textRecognitionModels)
    }


    private fun analyzeImage(image: Bitmap?) {
        if (image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        imageView.setImageBitmap(null)
        textRecognitionModels.clear()
        bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        showProgress()

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(image)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(firebaseVisionImage)
            .addOnSuccessListener {
                val mutableImage = image.copy(Bitmap.Config.ARGB_8888, true)

                recognizeText(it, mutableImage)

                imageView.setImageBitmap(mutableImage)
                hideProgress()
                bottomSheetRecyclerView.adapter?.notifyDataSetChanged()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            .addOnFailureListener {
                Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
                hideProgress()
            }


        cardSet = false
        invalidateOptionsMenu()
    }

    private fun recognizeText(result: FirebaseVisionText?, image: Bitmap?) {
        if (result == null || image == null) {
            Toast.makeText(this, "There was some error", Toast.LENGTH_SHORT).show()
            return
        }

        val canvas = Canvas(image)
        val rectPaint = Paint()
        rectPaint.color = Color.RED
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 4F
        val textPaint = Paint()
        textPaint.color = Color.RED
        textPaint.textSize = 40F

        var index = 0

        for (block in result.textBlocks) {
            for (line in block.lines) {

                canvas.drawRect(line.boundingBox!!, rectPaint)
                canvas.drawText(
                    index.toString(),
                    line.cornerPoints!![2].x.toFloat(),
                    line.cornerPoints!![2].y.toFloat(),
                    textPaint
                )
                textRecognitionModels.add(TextRecognitionModel(index++, line.text))


                if (!isNameSet) {
                    if (line.text.matches("[a-zA-Z .]+".toRegex())) {
                        nameString = line.text
                        isNameSet = true
                    }
                }



                if (!isEmailSet) {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(line.text).matches()) {
                        emailString = line.text
                        isEmailSet = true
                    }
                } else {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(line.text).matches()) {
                        altEmailString = line.text
                    }
                }

                if (line.text.contains("email", true) || line.text.contains("e-mail", true)) {
                    emailString = line.text.replace("email", "", true)
                    emailString = emailString.replace("e-mail", "", true)
                    emailString = emailString.replace(":", "", true)
                }

                if (line.text.contains("mobile", true) ||
                    line.text.contains("number", true) ||
                    line.text.contains("mobile number", true) ||
                    line.text.contains("mobile no", true) ||
                    line.text.contains("mobile no.", true) ||
                    line.text.contains("phone number", true) ||
                    line.text.contains("phone no", true) ||
                    line.text.contains("phone no.", true) ||
                    line.text.contains("phone", true) ||
                    line.text.contains("mob.", true)
                ) {
                    customphoneString = line.text.replace("mobile", "", true)
                    customphoneString = customphoneString.replace("mob", "", true)
                    customphoneString = customphoneString.replace("number", "", true)
                    customphoneString = customphoneString.replace("phone", "", true)
                    customphoneString = customphoneString.replace("no", "", true)
                    customphoneString = customphoneString.replace(":", "", true)
                    customphoneString = customphoneString.replace(".", "", true)
                    phoneString = customphoneString
                }


                if (!isPhoneSet) {
                    if (android.util.Patterns.PHONE.matcher(line.text).matches()) {
                        if (phoneString.isEmpty()) {
                            phoneString = line.text
                            isPhoneSet = true
                        } else {
                            phoneString = customphoneString
                            isPhoneSet = true
                        }
                    }
                }

                if (android.util.Patterns.PHONE.matcher(line.text).matches()) {
                    if (phoneString.isNotEmpty()) {
                        altPhoneString = line.text
                    }
                }





                if (android.util.Patterns.WEB_URL.matcher(line.text).matches()) {
                    websiteString = line.text
                } else {
                    val pattern = android.util.Patterns.WEB_URL
                    val matcher = pattern.matcher(line.text)
                    if (matcher.find()) {
                        websiteString = matcher.group(1)!!
                    }
                }
            }
        }
    }

    private fun showProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.GONE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.VISIBLE
    }

    private fun hideProgress() {
        findViewById<View>(R.id.bottom_sheet_button_image).visibility = View.VISIBLE
        findViewById<View>(R.id.bottom_sheet_button_progress).visibility = View.GONE
    }

}
