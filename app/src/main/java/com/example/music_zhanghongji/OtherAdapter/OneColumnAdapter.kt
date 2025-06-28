package com.example.music_zhanghongji.OtherAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_zhanghongji.MusicInfo
import com.example.music_zhanghongji.R

class OneColumnAdapter(
    private val list: List<MusicInfo>
) : RecyclerView.Adapter<OneColumnAdapter.OneColumnViewHolder>() {

    inner class OneColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneColumnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_one_column_image, parent, false)
        return OneColumnViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: OneColumnViewHolder, position: Int) {
        val music = list[position]

        Glide.with(holder.itemView).load(music.coverUrl).into(holder.imgCover)
        holder.tvTitle.text = music.musicName
        holder.tvAuthor.text = music.author

        holder.btnPlay.setOnClickListener {
            Toast.makeText(holder.itemView.context, "播放: ${music.musicName}", Toast.LENGTH_SHORT).show()
            // 播放逻辑
        }
    }
}
