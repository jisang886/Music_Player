package com.example.music_zhanghongji.api

import com.example.music_zhanghongji.HomePageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("music/homePage")
    suspend fun getHomePage(
        @Query("current") current: Int,
        @Query("size") size: Int = 5
    ): HomePageResponse
}