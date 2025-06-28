package com.example.music_zhanghongji.Banner

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
import com.example.music_zhanghongji.MusicInfo
import com.example.music_zhanghongji.R

class BannerPagerAdapter(
    private val list: List<MusicInfo>
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

        // 封面图圆角加载
        Glide.with(holder.itemView)
            .load(music.coverUrl)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusInPx)))
            .into(holder.imgCover)

        // 设置文本
        holder.tvTitle.text = music.musicName
        holder.tvAuthor.text = music.author

        // 播放按钮
        holder.btnPlay.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "播放：${music.musicName}",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: 添加播放逻辑
        }
    }
}
