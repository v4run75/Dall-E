package com.zetwerk.filemanager

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zetwerk.filemanager.utils.Constants
import com.zetwerk.filemanager.utils.FileManager

class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val imageView = findViewById<ImageView>(R.id.image_preview)

        val thumbnail = findViewById<View>(R.id.image_thumbnail)

        val file = intent.getStringExtra("file_uri")

        Glide.with(this)
            .load(FileManager.getBitmapFromUri(this, Uri.parse(file)))
            .timeout(Constants.GLIDE_TIME_OUT)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    thumbnail.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    thumbnail.visibility = View.GONE
                    return false
                }

            })
            .into(imageView)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}