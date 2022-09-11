package com.zetwerk.filemanager.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zetwerk.filemanager.R
import com.zetwerk.filemanager.models.FileModel
import com.zetwerk.filemanager.utils.FileManager
import com.zetwerk.filemanager.utils.FileManager.isPdf

enum class ThumbnailSize {
    SMALL, LARGE
}

class RecyclerViewAdapter(
    private val context: Context,
    private val modelFeedArrayList: ArrayList<FileModel>,
    private val listener: RecyclerViewItemClickListener,
    private val thumbnailSize: ThumbnailSize? = ThumbnailSize.SMALL
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface RecyclerViewItemClickListener {
        fun onItemClick(pos: Int)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_image_thumbnail, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, pos: Int) {

        val data = modelFeedArrayList[pos]
        val holder = viewHolder as ViewHolder

        holder.title.text = data.name

        thumbnailSize.let {
            if (it == ThumbnailSize.SMALL) {
                holder.container.layoutParams.height = dpToPixels(60)
                holder.container.layoutParams.width = dpToPixels(60)
            } else {
                holder.container.layoutParams.height = dpToPixels(100)
                holder.container.layoutParams.width = dpToPixels(100)
            }
        }

        if (!data.name.isPdf(context))
            holder.imageView.setImageBitmap(FileManager.getBitmapFromUri(context, data.uri))
        else
            FileManager.renderPdfFromUri(data.uri, context, holder.imageView)

        holder.container.setOnClickListener {
            listener.onItemClick(pos)
        }
    }

    private fun dpToPixels(dps: Int): Int {
        val scale: Float = context.resources.displayMetrics.density
        return (dps * scale + 0.5f).toInt()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return modelFeedArrayList.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.file_name)
        var imageView: ImageView = itemView.findViewById(R.id.file)
        var container: LinearLayout = itemView.findViewById(R.id.container)
    }
}