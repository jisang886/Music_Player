package com.example.music_zhanghongji.Banner

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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.music_zhanghongji.Activity.MusicPlayerActivity
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.MusicInfo
import com.example.music_zhanghongji.service.MusicService

class BannerPagerAdapter(
    private val list: List<MusicInfo>,
    private val musicBinder: MusicService.MusicBinder?
) : RecyclerView.Adapter<BannerPagerAdapter.BannerViewHolder>() {

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCover: ImageView = itemView.findViewById(R.id.imgBanner)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner_image, parent, false)
        return BannerViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val music = list[position]
        val radiusInPx = (16 * holder.itemView.resources.displayMetrics.density).toInt()

        Glide.with(holder.itemView)
            .load(music.coverUrl)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusInPx)))
            .into(holder.imgCover)

        holder.tvTitle.text = music.musicName
        holder.tvAuthor.text = music.author

        // 封面点击播放
        holder.imgCover.setOnClickListener {
            val context = holder.itemView.context
            if (musicBinder == null) {
                Toast.makeText(context, "播放器未准备好", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            musicBinder.playSingleMusic(music)

            val index = musicBinder.getCurrentIndex() ?: 0

            val intent = Intent(context, MusicPlayerActivity::class.java)
            intent.putExtra("startIndex", index)
            context.startActivity(intent)
        }


        // 播放列表添加按钮单独绑定点击事件
        holder.btnPlay.setOnClickListener {
            musicBinder?.addToPlayList(music)
            Toast.makeText(holder.itemView.context, "已加入播放列表: ${music.musicName}", Toast.LENGTH_SHORT).show()
        }
    }

}
