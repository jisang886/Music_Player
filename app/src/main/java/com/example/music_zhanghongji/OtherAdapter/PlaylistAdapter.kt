package com.example.music_zhanghongji.OtherAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.MusicInfo

class PlaylistAdapter(
    private val list: MutableList<MusicInfo>,
    private var currentIndex: Int,
    private val onClick: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvMusicTitle)
        val artist: TextView = itemView.findViewById(R.id.tvMusicArtist)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.musicName
        holder.artist.text = item.author

        // 设置背景（高亮当前播放项）
        holder.itemView.setBackgroundResource(
            if (position == currentIndex) R.drawable.rounded_corners_highlight
            else R.drawable.rounded_corners
        )

        // 设置点击监听
        holder.itemView.setOnClickListener {
            if (position != currentIndex) {
                val oldIndex = currentIndex
                currentIndex = position
                notifyItemChanged(oldIndex)
                notifyItemChanged(currentIndex)
            }
            onClick(position)
        }

        // 设置删除监听
        holder.btnDelete.setOnClickListener {
            val isDeletingCurrent = (position == currentIndex)
            list.removeAt(position)
            notifyItemRemoved(position)
            if (position < currentIndex) {
                currentIndex-- // 删除前面的项需要调整 currentIndex
            } else if (isDeletingCurrent) {
                currentIndex = -1 // 当前播放项被删掉，可重设为 -1 或 0
            }
            notifyItemRangeChanged(position, list.size - position)
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateCurrentIndex(index: Int) {
        if (index != currentIndex) {
            val oldIndex = currentIndex
            currentIndex = index
            notifyItemChanged(oldIndex)
            notifyItemChanged(currentIndex)
        }
    }



    fun updateData(newList: List<MusicInfo>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
