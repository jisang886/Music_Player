package com.example.music_zhanghongji

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.music_zhanghongji.OtherAdapter.MultiTypeAdapter
import com.example.music_zhanghongji.api.ApiClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private val adapter = MultiTypeAdapter()

    private var currentPage = 1
    private val pageSize = 5
    private var isLoading = false
    private var isLastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        swipeLayout.setOnRefreshListener {
            refreshData()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (dy <= 0) return

                val layoutManager = rv.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // 是否触发加载更多
                if (!isLoading && !isLastPage) {
                    val shouldLoadMore = visibleItemCount + firstVisibleItemPosition >= totalItemCount - 1 &&
                            firstVisibleItemPosition >= 0 &&
                            totalItemCount >= pageSize

                    if (shouldLoadMore) {
                        Log.d("分页加载", "触发加载更多: 当前页 $currentPage")
                        loadMoreData()
                    }
                }
            }
        })

        refreshData()
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        swipeLayout.isRefreshing = true
        loadData(currentPage, isRefresh = true)
    }

    private fun loadMoreData() {
        isLoading = true
        currentPage++
        loadData(currentPage, isRefresh = false)
    }

    private fun loadData(page: Int, isRefresh: Boolean) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.service.getHomePage(current = page, size = pageSize)
                val records = response.data.records
                val current = response.data.current
                val pages = response.data.pages

                if (isRefresh) {
                    adapter.setData(records)
                } else {
                    adapter.addData(records)
                }

                isLastPage = current >= pages

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "加载失败", Toast.LENGTH_SHORT).show()
                if (!isRefresh) currentPage-- // 加载失败时回退页码
            } finally {
                isLoading = false
                swipeLayout.isRefreshing = false
            }
        }
    }

}
