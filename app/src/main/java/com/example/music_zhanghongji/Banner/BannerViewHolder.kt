package com.example.music_zhanghongji.Banner

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.music_zhanghongji.model.MusicInfo
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.service.MusicService

class BannerViewHolder(
    itemView: View,
    private val musicBinder: MusicService.MusicBinder?  // 接收musicBinder
) : RecyclerView.ViewHolder(itemView) {

    private val viewPager = itemView.findViewById<ViewPager2>(R.id.viewPagerBanner)
    private val handler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private var currentPosition = 0
    private var realList = emptyList<MusicInfo>()

    fun bind(musicList: List<MusicInfo>) {
        realList = musicList

        if (realList.size <= 1) {
            viewPager.isUserInputEnabled = false
            viewPager.adapter = BannerPagerAdapter(realList, musicBinder) // 传入musicBinder
        } else {
            val loopList = createInfiniteList(realList)
            currentPosition = loopList.size / 2

            viewPager.adapter = BannerPagerAdapter(loopList, musicBinder) // 传入musicBinder
            viewPager.setCurrentItem(currentPosition, false)
            viewPager.isUserInputEnabled = true

            startAutoScroll()
        }
    }

    private fun startAutoScroll() {
        stopAutoScroll()
        autoScrollRunnable = object : Runnable {
            override fun run() {
                currentPosition++
                viewPager.setCurrentItem(currentPosition, true)
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(autoScrollRunnable!!, 3000)
    }

    fun stopAutoScroll() {
        autoScrollRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun createInfiniteList(list: List<MusicInfo>): List<MusicInfo> {
        val repeatFactor = 1000
        return List(repeatFactor * list.size) { index -> list[index % list.size] }
    }
}
