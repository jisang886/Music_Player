package com.example.music_zhanghongji.Activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.music_zhanghongji.OtherAdapter.MultiTypeAdapter
import com.example.music_zhanghongji.OtherAdapter.PlaylistAdapter
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.api.ApiClient
import com.example.music_zhanghongji.model.HomePageInfo
import com.example.music_zhanghongji.service.ACTION_PLAYBACK_STATE_CHANGED
import com.example.music_zhanghongji.service.EXTRA_IS_PLAYING
import com.example.music_zhanghongji.service.MusicService
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), MusicService.PlaybackStateListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout
    private val adapter = MultiTypeAdapter()

    private lateinit var floatingPlayerView: LinearLayout
    private lateinit var ivCover: ImageView
    private lateinit var tvSongTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnPlayPauseFloating: ImageButton
    private lateinit var btnPlaylistFloating: ImageButton

    private var musicBinder: MusicService.MusicBinder? = null
    private var isBound = false

    private var currentPage = 1
    private val pageSize = 5
    private var isLoading = false
    private var isLastPage = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicBinder = service as? MusicService.MusicBinder
            isBound = true
            adapter.setMusicBinder(musicBinder)
            musicBinder?.setPlaybackStateListener(this@MainActivity)
            updateFloatingPlayer() // 初始化更新状态
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicBinder?.setPlaybackStateListener(null)
            musicBinder = null
            isBound = false
            adapter.setMusicBinder(null)
            showDefaultFloatingPlayer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)

        floatingPlayerView = findViewById(R.id.floatingPlayerView)
        ivCover = findViewById(R.id.ivCover)
        tvSongTitle = findViewById(R.id.tvSongTitle)
        tvArtist = findViewById(R.id.tvArtist)
        btnPlayPauseFloating = findViewById(R.id.btnPlayPauseFloating)
        btnPlaylistFloating = findViewById(R.id.btnPlaylistFloating)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        swipeLayout.setOnRefreshListener { refreshData() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                if (dy <= 0) return

                val layoutManager = rv.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    val shouldLoadMore = visibleItemCount + firstVisibleItemPosition >= totalItemCount - 1 &&
                            firstVisibleItemPosition >= 0 &&
                            totalItemCount >= pageSize

                    if (shouldLoadMore) {
                        loadMoreData()
                    }
                }
            }
        })

        // 关键修改：播放/暂停按钮点击后立刻切换图标，避免等待广播延迟
        btnPlayPauseFloating.setOnClickListener {
            musicBinder?.let {
                if (it.isPlaying()) {
                    it.pause()
                    updateFloatingPlayer(false)
                } else {
                    it.play()
                    updateFloatingPlayer(true)
                }
            }
        }

        btnPlaylistFloating.setOnClickListener {
            showPlaylistDialog()
        }

        floatingPlayerView.setOnClickListener {
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("startIndex", musicBinder?.getCurrentIndex() ?: 0)
            startActivity(intent)
        }

        Intent(this, MusicService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }

        refreshData()
    }

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_PLAYBACK_STATE_CHANGED) {
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                runOnUiThread {
                    updateFloatingPlayer(isPlaying)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            playbackReceiver,
            IntentFilter(ACTION_PLAYBACK_STATE_CHANGED),
            Context.RECEIVER_NOT_EXPORTED
        )
        updateFloatingPlayer()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(playbackReceiver)
    }

    private fun refreshData() {
        currentPage = 1
        isLastPage = false
        swipeLayout.isRefreshing = true
        loadData(currentPage, true)
    }

    private fun loadMoreData() {
        isLoading = true
        currentPage++
        loadData(currentPage, false)
    }

    private fun loadData(page: Int, isRefresh: Boolean) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.service.getHomePage(current = page, size = pageSize)
                val records: List<HomePageInfo> = response.data.records
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
                if (!isRefresh) currentPage--
            } finally {
                isLoading = false
                swipeLayout.isRefreshing = false
            }
        }
    }

    /**
     * 更新浮动播放器UI
     * @param isPlayingFromBroadcast 可选：从广播中接收到的播放状态，用于避免读取状态延迟
     */
    private fun updateFloatingPlayer(isPlayingFromBroadcast: Boolean? = null) {
        val playList = musicBinder?.getPlayList() ?: emptyList()
        val index = musicBinder?.getCurrentIndex() ?: 0

        if (playList.isEmpty()) {
            showDefaultFloatingPlayer()
            return
        }

        val currentMusic = playList.getOrNull(index)
        if (currentMusic == null) {
            showDefaultFloatingPlayer()
            return
        }

        floatingPlayerView.visibility = View.VISIBLE
        tvSongTitle.text = currentMusic.musicName
        tvArtist.text = currentMusic.author

        Glide.with(this)
            .load(currentMusic.coverUrl)
            .placeholder(R.drawable.ic_music_placeholder)
            .circleCrop()
            .into(ivCover)

        // 优先使用传入参数，避免播放状态判断延迟
        val isPlaying = isPlayingFromBroadcast ?: (musicBinder?.isPlaying() ?: false)
        btnPlayPauseFloating.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    private fun showDefaultFloatingPlayer() {
        floatingPlayerView.visibility = View.VISIBLE
        tvSongTitle.text = "无播放歌曲"
        tvArtist.text = ""
        ivCover.setImageResource(R.drawable.ic_music_placeholder)
        btnPlayPauseFloating.setImageResource(R.drawable.ic_play)
    }

    private fun showPlaylistDialog() {
        val playList = musicBinder?.getPlayList() ?: return
        if (playList.isEmpty()) return

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_playlist, null)

        val tvCount = view.findViewById<TextView>(R.id.tvCount)
        val rv = view.findViewById<RecyclerView>(R.id.rvPlaylist)

        tvCount.text = "播放列表（${playList.size}首）"

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = PlaylistAdapter(
            playList.toMutableList(),
            musicBinder?.getCurrentIndex() ?: 0,
            onClick = { index ->
                val music = playList.getOrNull(index) ?: return@PlaylistAdapter
                musicBinder?.playSingleMusic(music)
                updateFloatingPlayer()
                dialog.dismiss()
            },
            onDelete = { index ->
                musicBinder?.removeAt(index)
                updateFloatingPlayer()
                dialog.dismiss()
            },

        )
        rv.adapter = adapter

        dialog.setContentView(view)
        dialog.show()
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        runOnUiThread {
            updateFloatingPlayer(isPlaying)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            musicBinder?.setPlaybackStateListener(null)
            unbindService(connection)
            isBound = false
        }
    }
}
