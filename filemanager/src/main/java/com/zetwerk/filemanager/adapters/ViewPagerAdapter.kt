package com.zetwerk.filemanager.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.zetwerk.filemanager.utils.FileManager
import com.zetwerk.filemanager.utils.FileManager.isPdf
import com.zetwerk.filemanager.models.FileModel
import com.zetwerk.filemanager.R


class ViewPagerAdapter(val context: Context, val files: ArrayList<FileModel>?,val listener: OnViewPagerItemClickListener) : PagerAdapter() {

    interface OnViewPagerItemClickListener {
        fun onItemClick(file: FileModel, view: View)
    }

    lateinit var mLayoutInflater: LayoutInflater

    override fun getCount(): Int {
        return files?.size ?: 0
    }

    override fun isViewFromObject(view: View, viewObject: Any): Boolean {
        return view === viewObject as View
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View = mLayoutInflater.inflate(
            R.layout.list_item_image_preview,
            container, false
        )
        val imageView: ImageView = itemView.findViewById(R.id.image_preview) as ImageView


        files?.let {
            val file = it[position]

            if (!file.name.isPdf(context))
                imageView.setImageBitmap(FileManager.getBitmapFromUri(context, file.uri))
            else
                FileManager.renderPdfFromUri(file.uri, context, imageView)


            itemView.setOnClickListener { view ->
                listener.onItemClick(file, view)
            }
        }

        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, viewObject: Any) {
        container.removeView(viewObject as View?)
    }

}