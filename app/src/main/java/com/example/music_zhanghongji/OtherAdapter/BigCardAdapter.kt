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

class BigCardAdapter(
    private val list: List<MusicInfo>,
    private val musicBinder: MusicService.MusicBinder?
) : RecyclerView.Adapter<BigCardAdapter.BigCardViewHolder>() {

    inner class BigCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_big_card_image, parent, false)
        return BigCardViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BigCardViewHolder, position: Int) {
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

            musicBinder.playSingleMusic(music)
            musicBinder.play()  // 关键：调用播放

            val index = musicBinder.getCurrentIndex()

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
