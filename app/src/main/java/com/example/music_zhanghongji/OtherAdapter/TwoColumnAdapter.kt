package com.example.music_zhanghongji.OtherAdapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_zhanghongji.Activity.MusicPlayerActivity
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.MusicInfo
import com.example.music_zhanghongji.service.MusicService

class TwoColumnAdapter(
    private val list: List<MusicInfo>,
    private val musicBinder: MusicService.MusicBinder?
) : RecyclerView.Adapter<TwoColumnAdapter.TwoColumnViewHolder>() {

    inner class TwoColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwoColumnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_two_column_image, parent, false)
        return TwoColumnViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: TwoColumnViewHolder, position: Int) {
        val music = list[position]

        Glide.with(holder.itemView).load(music.coverUrl).into(holder.imgCover)
        holder.tvTitle.text = music.musicName
        holder.tvAuthor.text = music.author

        holder.imgCover.setOnClickListener {
            val context = holder.itemView.context
            if (musicBinder == null) {
                Toast.makeText(context, "播放器未准备好", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 直接调用 playSingleMusic 播放这首歌
            musicBinder.playSingleMusic(music)
            musicBinder.play()  // 确保调用 play 开始播放

            val index = musicBinder.getCurrentIndex()

            // 跳转播放页时传递当前索引
            val intent = Intent(context, MusicPlayerActivity::class.java)
            intent.putExtra("startIndex", index)
            context.startActivity(intent)
        }

        holder.btnPlay.setOnClickListener {
            musicBinder?.addToPlayList(music)
            Toast.makeText(holder.itemView.context, "已加入播放列表: ${music.musicName}", Toast.LENGTH_SHORT).show()
        }
    }

}
