package com.example.music_zhanghongji.model



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
