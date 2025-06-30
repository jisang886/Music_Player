package com.example.music_zhanghongji.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicInfo(
    val id: Long,
    val musicName: String,
    val author: String,
    val coverUrl: String,
    val musicUrl: String,
    val lyricUrl: String
) : Parcelable

