package com.example.music_zhanghongji.OtherAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.music_zhanghongji.Banner.BannerViewHolder
import com.example.music_zhanghongji.Banner.BannerPagerAdapter
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.HomePageInfo
import com.example.music_zhanghongji.model.MusicInfo
import com.example.music_zhanghongji.service.MusicService

class MultiTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HomePageInfo>()
    private var musicBinder: MusicService.MusicBinder? = null

    fun setMusicBinder(binder: MusicService.MusicBinder?) {
        musicBinder = binder
        notifyDataSetChanged()
    }

    fun setData(newData: List<HomePageInfo>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    fun addData(moreData: List<HomePageInfo>) {
        val startPos = items.size
        items.addAll(moreData)
        notifyItemRangeInserted(startPos, moreData.size)
    }

    override fun getItemViewType(position: Int): Int = items[position].style

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> {
                val view = inflater.inflate(R.layout.item_banner, parent, false)
                BannerViewHolder(view, musicBinder)  // 这里传入 musicBinder
            }
            2 -> {
                val view = inflater.inflate(R.layout.item_big_card, parent, false)
                BigCardViewHolder(view)
            }
            3 -> {
                val view = inflater.inflate(R.layout.item_one_column, parent, false)
                OneColumnViewHolder(view)
            }
            4 -> {
                val view = inflater.inflate(R.layout.item_two_column, parent, false)
                TwoColumnViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is BannerViewHolder -> holder.bind(item.musicInfoList)
            is BigCardViewHolder -> holder.bind(item.musicInfoList)
            is OneColumnViewHolder -> holder.bind(item.musicInfoList)
            is TwoColumnViewHolder -> holder.bind(item.musicInfoList)
        }
    }

    inner class BigCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewBigCard)
        fun bind(musicList: List<MusicInfo>) {
            recyclerView.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = BigCardAdapter(musicList, musicBinder)
        }
    }

    inner class OneColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewOneColumn)
        fun bind(musicList: List<MusicInfo>) {
            recyclerView.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = OneColumnAdapter(musicList, musicBinder)
        }
    }

    inner class TwoColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewTwoColumn)
        fun bind(musicList: List<MusicInfo>) {
            recyclerView.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = TwoColumnAdapter(musicList, musicBinder)
        }
    }
}
