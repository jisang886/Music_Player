package com.example.music_zhanghongji.OtherAdapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.music_zhanghongji.Banner.BannerPagerAdapter
import com.example.music_zhanghongji.HomePageInfo
import com.example.music_zhanghongji.MusicInfo
import com.example.music_zhanghongji.R

class MultiTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<HomePageInfo>()

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

    override fun getItemViewType(position: Int): Int {
        return items[position].style
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> {
                val view = inflater.inflate(R.layout.item_banner, parent, false)
                BannerViewHolder(view)
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

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is BannerViewHolder -> holder.bind(item.musicInfoList)
            is BigCardViewHolder -> holder.bind(item.musicInfoList)
            is OneColumnViewHolder -> holder.bind(item.musicInfoList)
            is TwoColumnViewHolder -> holder.bind(item.musicInfoList)
        }
    }

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPagerBanner)
        private val handler = Handler(Looper.getMainLooper())
        private var runnable: Runnable? = null
        private var currentPosition = 0
        private var bannerList = listOf<MusicInfo>()

        fun bind(musicList: List<MusicInfo>) {
            bannerList = musicList

            stopAutoSlide()

            if (bannerList.size == 1) {
                viewPager.isUserInputEnabled = false
                viewPager.adapter = BannerPagerAdapter(bannerList)
            } else if (bannerList.size > 1) {
                val infiniteList = createInfiniteList(bannerList)
                viewPager.isUserInputEnabled = true
                viewPager.adapter = BannerPagerAdapter(infiniteList)
                currentPosition = infiniteList.size / 2
                viewPager.setCurrentItem(currentPosition, false)

                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        currentPosition = position
                    }
                })

                startAutoSlide()
            } else {
                viewPager.adapter = null
            }
        }

        private fun startAutoSlide() {
            runnable?.let { handler.removeCallbacks(it) }
            runnable = object : Runnable {
                override fun run() {
                    currentPosition++
                    viewPager.setCurrentItem(currentPosition, true)
                    handler.postDelayed(this, 3000)
                }
            }
            handler.postDelayed(runnable!!, 3000)
        }

        fun stopAutoSlide() {
            runnable?.let { handler.removeCallbacks(it) }
        }

        private fun createInfiniteList(list: List<MusicInfo>): List<MusicInfo> {
            val times = 1000
            val result = mutableListOf<MusicInfo>()
            repeat(times) {
                result.addAll(list)
            }
            return result
        }
    }

    inner class BigCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewBigCard)
        fun bind(musicList: List<MusicInfo>) {
            recyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = BigCardAdapter(musicList)
        }
    }

    inner class OneColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewOneColumn)
        fun bind(musicList: List<MusicInfo>) {
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
            recyclerView.adapter = OneColumnAdapter(musicList)
        }
    }

    inner class TwoColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView = itemView.findViewById<RecyclerView>(R.id.recyclerViewTwoColumn)
        fun bind(musicList: List<MusicInfo>) {
            recyclerView.layoutManager = GridLayoutManager(itemView.context, 2)
            recyclerView.adapter = TwoColumnAdapter(musicList)
        }
    }
}
