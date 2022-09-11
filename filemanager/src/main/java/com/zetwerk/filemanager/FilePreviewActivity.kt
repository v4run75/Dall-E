package com.zetwerk.filemanager

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.zetwerk.filemanager.adapters.RecyclerViewAdapter
import com.zetwerk.filemanager.adapters.ThumbnailSize
import com.zetwerk.filemanager.adapters.ViewPagerAdapter
import com.zetwerk.filemanager.databinding.ActivityImagePickerBinding
import com.zetwerk.filemanager.models.FileModel
import com.zetwerk.filemanager.utils.FileManager

class FilePreviewActivity : AppCompatActivity() {
    lateinit var dataBinding: ActivityImagePickerBinding
    var filesList = ArrayList<FileModel>()
    private var recyclerViewAdapter: RecyclerViewAdapter? = null
    private var viewPagerAdapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_picker)

        clickListeners()
        setAdapters()
    }

    private fun setAdapters() {
        setRecyclerViewAdapter()
    }

    val viewPagerListener = object : ViewPagerAdapter.OnViewPagerItemClickListener{
        override fun onItemClick(file: FileModel, view: View) {
            FileManager.openFile(file.uri, file.name, this@FilePreviewActivity, view)
        }
    }

    private fun setViewPagerAdapter() {
        val viewPager =
            dataBinding.layoutDisplayImage.rootView.findViewById<ViewPager>(R.id.view_pager)
        viewPagerAdapter = ViewPagerAdapter(this, filesList, viewPagerListener)
        viewPager.adapter = viewPagerAdapter
        viewPager.currentItem = (viewPagerAdapter?.count)?.minus(1) ?: 0
    }

    private val listener = object : RecyclerViewAdapter.RecyclerViewItemClickListener {
        override fun onItemClick(pos: Int) {
            dataBinding.layoutDisplayImage.rootView.findViewById<ViewPager>(R.id.view_pager).currentItem =
                pos
        }
    }

    private fun setRecyclerViewAdapter() {
        val recyclerView =
            dataBinding.layoutImageList.rootView.findViewById<RecyclerView>(R.id.rv_files)
        recyclerView?.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerViewAdapter = RecyclerViewAdapter(this, filesList, listener, ThumbnailSize.LARGE)
        recyclerViewAdapter?.let {
            recyclerView?.adapter = it
        }
    }

    private fun clickListeners() {
        dataBinding.layoutImageList.rootView.findViewById<ImageView>(R.id.addItem)
            .setOnClickListener {
                FileManager.openFileForResult(fileResultLauncher)
            }
    }


    private var fileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val fileMap = FileManager.handleOpenMultipleFileResult(result, this.contentResolver)

            fileMap?.let {
                it.forEach { file ->
                    filesList.add(FileModel(file.key, file.value))
                    recyclerViewAdapter?.notifyItemInserted(filesList.size)
                }
            }
            setViewPagerAdapter()
        }


}
