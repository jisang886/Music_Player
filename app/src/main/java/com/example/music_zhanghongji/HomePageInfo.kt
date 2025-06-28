package com.example.music_zhanghongji

data class MusicInfo(
    val id: Long,
    val musicName: String,
    val author: String,
    val coverUrl: String,
    val musicUrl: String,
    val lyricUrl: String // 别忘了歌词链接
)

data class HomePageInfo(
    val moduleConfigId: Int,
    val moduleName: String,
    val style: Int,
    val musicInfoList: List<MusicInfo>
)

data class HomePageResponse(
    val code: Int,
    val msg: String,
    val data: PageData
)

data class PageData(
    val records: List<HomePageInfo>,
    val current: Int,
    val size: Int,
    val total: Int,
    val pages: Int
)
