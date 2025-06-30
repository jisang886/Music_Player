package com.example.music_zhanghongji.OtherAdapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.LyricLine
import com.example.music_zhanghongji.model.MusicInfo
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import java.net.URL

class MusicPagerAdapter(
    private val context: Context,
    private val musicList: List<MusicInfo>,
    private var currentIndex: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isPlaying = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun getItemCount(): Int = 2

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == 0) R.layout.page_music_cover else R.layout.page_music_lyric
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return if (viewType == 0) CoverViewHolder(view) else LyricViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val music = musicList.getOrNull(currentIndex) ?: return
        if (holder is CoverViewHolder) holder.bind(music, isPlaying)
        if (holder is LyricViewHolder) holder.bind(music)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (position == 1 && holder is LyricViewHolder) {
                val ms = payloads[0] as? Int ?: return
                holder.updateCurrentTime(ms)
            }
        }
    }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        mainHandler.post {
            notifyItemChanged(0)
        }
    }

    fun updateCurrentIndex(index: Int) {
        currentIndex = index
        mainHandler.post {
            notifyDataSetChanged()
        }
    }

    fun updateLyricProgress(ms: Int) {
        mainHandler.post {
            notifyItemChanged(1, ms)
        }
    }

    inner class CoverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCover: ImageView = itemView.findViewById(R.id.imgRotatingCover)

        private var rotationAnimator: ObjectAnimator? = null
        private var currentRotation = 0f

        fun bind(music: MusicInfo, isPlaying: Boolean) {
            Glide.with(context).load(music.coverUrl).into(imgCover)

            if (rotationAnimator == null) {
                rotationAnimator = ObjectAnimator.ofFloat(imgCover, "rotation", 0f, 360f).apply {
                    duration = 10000L
                    interpolator = LinearInterpolator()
                    repeatCount = ObjectAnimator.INFINITE
                }
            } else {
                // 保证动画起点是当前角度
                rotationAnimator?.cancel()
                currentRotation = imgCover.rotation % 360
                rotationAnimator?.setFloatValues(currentRotation, currentRotation + 360f)
            }

            if (isPlaying) {
                if (rotationAnimator?.isStarted != true) {
                    rotationAnimator?.start()
                } else {
                    rotationAnimator?.resume()
                }
            } else {
                // 暂停动画同时保存当前角度
                rotationAnimator?.let {
                    currentRotation = imgCover.rotation % 360
                    it.pause()
                }
            }
        }
    }

    inner class LyricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.rvLyric)
        private val adapter = LyricAdapter()
        private var lyrics: List<LyricLine> = emptyList()

        init {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        }

        fun bind(music: MusicInfo) {
            adapter.setLyrics(emptyList())  // 清空旧歌词
            Thread {
                try {
                    val content = URL(music.lyricUrl).readText(charset("UTF-8"))
                    val parsed = parseLyric(content)
                    lyrics = parsed
                    (itemView.context as? Activity)?.runOnUiThread {
                        adapter.setLyrics(parsed)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        fun updateCurrentTime(ms: Int) {
            if (lyrics.isEmpty()) return
            val index = lyrics.indexOfLast { it.time <= ms }
            adapter.updateHighlight(index)
            if (index >= 0) {
                (recyclerView.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(index, recyclerView.height / 2)
            }
        }
    }
}

// 解析歌词函数，解析LRC格式，返回LyricLine列表
private fun parseLyric(content: String): List<LyricLine> {
    val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]""")
    val result = mutableListOf<LyricLine>()

    content.lines().forEach { line ->
        val matches = regex.findAll(line)
        val text = line.replace(regex, "").trim()
        for (m in matches) {
            val min = m.groupValues[1].toIntOrNull() ?: 0
            val sec = m.groupValues[2].toIntOrNull() ?: 0
            val millisStr = m.groupValues[3]
            val millis = if (millisStr.length == 2) (millisStr.toIntOrNull() ?: 0) * 10 else (millisStr.toIntOrNull() ?: 0)
            val timeMs = min * 60 * 1000 + sec * 1000 + millis
            result.add(LyricLine(timeMs, text))
        }
    }

    return result.sortedBy { it.time }
}
