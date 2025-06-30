package com.example.music_zhanghongji.OtherAdapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.LyricLine

class LyricAdapter : RecyclerView.Adapter<LyricAdapter.LyricViewHolder>() {
    private var lyrics: List<LyricLine> = emptyList()
    private var highlightedIndex = -1

    fun setLyrics(list: List<LyricLine>) {
        lyrics = list
        highlightedIndex = -1
        notifyDataSetChanged()
    }

    fun updateHighlight(newIndex: Int) {
        if (newIndex != highlightedIndex) {
            val oldIndex = highlightedIndex
            highlightedIndex = newIndex
            if (oldIndex >= 0) notifyItemChanged(oldIndex)
            if (newIndex >= 0) notifyItemChanged(newIndex)
        }
    }

    override fun getItemCount(): Int = lyrics.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lyric_line, parent, false)
        return LyricViewHolder(view)
    }

    override fun onBindViewHolder(holder: LyricViewHolder, position: Int) {
        val lyric = lyrics[position]
        holder.bind(lyric.text, position == highlightedIndex)
    }

    class LyricViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLine: TextView = itemView.findViewById(R.id.tvLyricLine)

        fun bind(text: String, isHighlighted: Boolean) {
            tvLine.text = text
            tvLine.setTextColor(if (isHighlighted) Color.parseColor("#FF4081") else Color.BLACK)
            tvLine.textSize = if (isHighlighted) 18f else 16f
        }
    }
}
