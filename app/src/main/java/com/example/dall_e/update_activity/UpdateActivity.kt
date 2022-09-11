package com.example.dall_e.update_activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.dall_e.FirebaseRecognition.TextRecognitionActivity
import com.example.dall_e.databinding.ActivityUpdateBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.zetwerk.filemanager.utils.FileManager

class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding
    var appCompatActivity: AppCompatActivity = this

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Snackbar.make(binding.container, "Files Added", BaseTransientBottomBar.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar

        binding.upload.setOnClickListener {
            FileManager.openFileForResult(launcher, true, false)
        }
        binding.scan.setOnClickListener {
            startActivity(Intent(this, TextRecognitionActivity::class.java))
        }

        binding.submit.setOnClickListener {
            finish()
        }


    }


}