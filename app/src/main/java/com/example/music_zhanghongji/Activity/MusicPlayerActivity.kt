package com.example.music_zhanghongji.Activity

import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.material.R as MaterialR
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.music_zhanghongji.OtherAdapter.MusicPagerAdapter
import com.example.music_zhanghongji.OtherAdapter.PlaylistAdapter
import com.example.music_zhanghongji.R
import com.example.music_zhanghongji.model.MusicInfo
import com.example.music_zhanghongji.model.PlayMode
import com.example.music_zhanghongji.service.ACTION_PLAYBACK_STATE_CHANGED
import com.example.music_zhanghongji.service.EXTRA_IS_PLAYING
import com.example.music_zhanghongji.service.MusicService
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.concurrent.fixedRateTimer

class MusicPlayerActivity : AppCompatActivity(), MusicService.PlaybackStateListener {

    private var playlistDialogAdapter: PlaylistAdapter? = null
    private var playlistDialog: BottomSheetDialog? = null

    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnClose: ImageButton
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPlaylist: ImageButton
    private lateinit var btnPlayMode: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvTitleTop: TextView
    private lateinit var tvArtistTop: TextView
    private lateinit var layoutRoot: ConstraintLayout
    private lateinit var layoutSongInfo: LinearLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btnFavorite: ImageButton

    private var musicBinder: MusicService.MusicBinder? = null
    private var isBound = false
    private var isUserSeeking = false
    private var currentPlayMode = PlayMode.ORDER
    private var updateTimer: java.util.Timer? = null

    private val prefs by lazy { getSharedPreferences("favorites", Context.MODE_PRIVATE) }
    private var isFavorite = false

    private var lastIndex = -1

    private lateinit var adapter: MusicPagerAdapter

    private var isUserClickSwitch = false
    private var isPageSelectedLocked = false

    private var playList: List<MusicInfo> = emptyList()
    private var currentIndex = 0
    private var currentPage = 0  // 0: 封面页，1: 歌词页
    private val swipeThreshold = 300f
    private lateinit var gestureDetector: GestureDetector
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicBinder = service as MusicService.MusicBinder
            isBound = true
            musicBinder!!.setPlaybackStateListener(this@MusicPlayerActivity)

            playList = musicBinder!!.getPlayList()
            if (playList.isEmpty()) return finish()

            currentIndex = musicBinder!!.getCurrentIndex().coerceIn(playList.indices)

            adapter = MusicPagerAdapter(this@MusicPlayerActivity, playList, currentIndex)
            viewPager.adapter = adapter
            viewPager.setCurrentItem(currentIndex, false)

            currentPlayMode = musicBinder!!.getPlayMode()
            updateUI()
            updatePlayModeUI()
            startProgressUpdater()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicBinder = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        initViews()
        bindService(Intent(this, MusicService::class.java), connection, Context.BIND_AUTO_CREATE)
        setupListeners()
        setupGesture()
    }

    private fun initViews() {
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnClose = findViewById(R.id.btnClose)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        btnPlaylist = findViewById(R.id.btnPlaylist)
        btnPlayMode = findViewById(R.id.btnPlayMode)
        seekBar = findViewById(R.id.seekBar)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)
        tvTotalTime = findViewById(R.id.tvTotalTime)
        tvTitleTop = findViewById(R.id.tvTitleTop)
        tvArtistTop = findViewById(R.id.tvArtistTop)
        layoutRoot = findViewById(R.id.layout_music_player)
        layoutSongInfo = findViewById(R.id.layout_song_info)
        viewPager = findViewById(R.id.viewPager)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnPlayPause.setImageResource(R.drawable.ic_play)
    }

    private fun setupGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                layoutRoot.animate().cancel()
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                val deltaY = e2.y - (e1?.y ?: 0f)
                if (deltaY > 0) {
                    layoutRoot.translationY = deltaY
                    return true
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            if (layoutRoot.translationY > swipeThreshold) {
                layoutRoot.animate().translationY(layoutRoot.height.toFloat()).setDuration(300)
                    .withEndAction {
                        finish()
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    }.start()
            } else {
                layoutRoot.animate().translationY(0f).setDuration(300).start()
            }
        }
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun setupListeners() {
        btnClose.setOnClickListener { finish() }
        btnPlayPause.setOnClickListener {
            musicBinder?.let { if (it.isPlaying()) it.pause() else it.play() }
        }

        btnNext.setOnClickListener {
            musicBinder?.playNext()
            // 不要在这里 updateUI，等播放状态回调触发
        }
        btnPrev.setOnClickListener {
            musicBinder?.playPrevious()
        }



        btnPlaylist.setOnClickListener { showPlaylistDialog() }
        btnPlayMode.setOnClickListener {
            currentPlayMode = when (currentPlayMode) {
                PlayMode.ORDER -> PlayMode.RANDOM
                PlayMode.RANDOM -> PlayMode.SINGLE_LOOP
                PlayMode.SINGLE_LOOP -> PlayMode.ORDER
            }
            musicBinder?.setPlayMode(currentPlayMode)
            updatePlayModeUI()
        }
        btnFavorite.setOnClickListener { toggleFavorite() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(sb: SeekBar?) { isUserSeeking = true }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                isUserSeeking = false
                sb?.let { musicBinder?.seekTo(it.progress) }
            }
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) tvCurrentTime.text = formatTime(progress)
            }
        })

        // 绑定适配器和回调，等待 playList 赋值后再用 service 赋值
        // 这里初始可空适配器，后续 service 连接回调赋值
        adapter = MusicPagerAdapter(this, playList, currentIndex)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                if (position == 1) {
                    val currentMs = musicBinder?.getCurrentPosition() ?: 0
                    viewPager.post {
                        adapter.updateLyricProgress(currentMs)
                    }
                }
            }
        })

    }

    private fun updatePlayModeUI() {
        val iconRes = when (currentPlayMode) {
            PlayMode.ORDER -> R.drawable.ic_order_play
            PlayMode.RANDOM -> R.drawable.ic_random_play
            PlayMode.SINGLE_LOOP -> R.drawable.ic_single_loop
        }
        btnPlayMode.setImageResource(iconRes)
    }

    private fun updateUI() {
        val playing = musicBinder?.isPlaying() ?: false
        btnPlayPause.setImageResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play)

        val duration = musicBinder?.getDuration() ?: 0
        seekBar.max = duration
        tvTotalTime.text = formatTime(duration)

        val pos = musicBinder?.getCurrentPosition() ?: 0
        if (!isUserSeeking) {
            seekBar.progress = pos
            tvCurrentTime.text = formatTime(pos)
        }

        playList.getOrNull(currentIndex)?.let { m ->
            tvTitleTop.text = m.musicName
            tvArtistTop.text = m.author
            loadFavoriteState(m)

            Glide.with(this).asBitmap().load(m.coverUrl).circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(res: Bitmap, trans: Transition<in Bitmap>?) {
                        val color = Palette.from(res).generate().getVibrantColor(0xFF888888.toInt())
                        layoutRoot.setBackgroundColor(color)
                        layoutSongInfo.setBackgroundColor(color)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        adapter.updateCurrentIndex(currentIndex)
        adapter.setPlaying(playing)

        // **不要调用 viewPager.setCurrentItem(currentIndex, false)**
        // 保持当前页面 currentPage 不变

        if (currentPage == 1) {
            val currentMs = musicBinder?.getCurrentPosition() ?: 0
            adapter.updateLyricProgress(currentMs)
        }

    }


    private fun toggleFavorite() {
        val music = playList.getOrNull(currentIndex) ?: return
        isFavorite = !isFavorite
        updateFavoriteUI()

        if (isFavorite) {
            btnFavorite.animate()
                .scaleX(1.2f).scaleY(1.2f)
                .rotationYBy(360f)
                .setDuration(1000)
                .withEndAction {
                    btnFavorite.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        } else {
            btnFavorite.animate()
                .scaleX(0.8f).scaleY(0.8f)
                .setDuration(1000)
                .withEndAction {
                    btnFavorite.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        saveFavoriteState(music)
    }

    private fun updateFavoriteUI() {
        btnFavorite.setImageResource(if (isFavorite) R.drawable.ic_favorite_on else R.drawable.ic_favorite_off)
    }

    private fun saveFavoriteState(music: MusicInfo) {
        prefs.edit().putBoolean(music.musicName.toString(), isFavorite).apply()
    }

    private fun loadFavoriteState(music: MusicInfo) {
        isFavorite = prefs.getBoolean(music.musicName.toString(), false)
        updateFavoriteUI()
    }

    private val refreshRunnable = Runnable {
        if (lastIndex != -1) adapter.notifyItemChanged(lastIndex)
        if (currentIndex != -1) adapter.notifyItemChanged(currentIndex)

        adapter.updateCurrentIndex(currentIndex)
        viewPager.setCurrentItem(currentIndex, false)
        updateUI()

        lastIndex = currentIndex
        isPageSelectedLocked = false
    }

    private fun refreshAfterChange() {
        if (!isBound) return
        val newIndex = musicBinder?.getCurrentIndex() ?: 0
        playList = musicBinder?.getPlayList() ?: emptyList()

        currentIndex = newIndex

        if (isPageSelectedLocked) {
            handler.removeCallbacks(refreshRunnable)
        }
        isPageSelectedLocked = true
        handler.post(refreshRunnable)
    }

    private fun startProgressUpdater() {
        updateTimer?.cancel()
        updateTimer = fixedRateTimer("progress", true, 0, 500) {
            if (!isUserSeeking && isBound) runOnUiThread {
                musicBinder?.let {
                    val dur = it.getDuration()
                    val cur = it.getCurrentPosition()
                    seekBar.max = dur
                    seekBar.progress = cur
                    tvCurrentTime.text = formatTime(cur)
                    tvTotalTime.text = formatTime(dur)
                    adapter.updateLyricProgress(cur)
                }
            }
        }
    }

    private fun onSongClick(index: Int) {
        currentIndex = index
        viewPager.setCurrentItem(currentIndex, false)
        musicBinder?.playAt(currentIndex)
        musicBinder?.play()
        updateUI()
    }

    private fun showPlaylistDialog() {
        val playList = musicBinder?.getPlayList() ?: return
        if (playList.isEmpty()) return

        playlistDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_playlist, null)

        val tvCount = view.findViewById<TextView>(R.id.tvCount)
        val rv = view.findViewById<RecyclerView>(R.id.rvPlaylist)

        tvCount.text = "播放列表（${playList.size}首）"

        rv.layoutManager = LinearLayoutManager(this)
        playlistDialogAdapter = PlaylistAdapter(
            playList.toMutableList(),
            musicBinder?.getCurrentIndex() ?: 0,
            onClick = { index ->
                onSongClick(index)
                playlistDialog?.dismiss()
            },
            onDelete = { index ->
                musicBinder?.removeAt(index)
                refreshAfterChange()
                if (musicBinder?.getPlayList()?.isEmpty() == true) {
                    playlistDialog?.dismiss()
                    finish()
                } else {
                    playlistDialogAdapter?.updateData(
                        musicBinder?.getPlayList()?.toMutableList() ?: mutableListOf()
                    )
                    playlistDialogAdapter?.updateCurrentIndex(
                        musicBinder?.getCurrentIndex() ?: 0
                    )
                    tvCount.text = "播放列表（${musicBinder?.getPlayList()?.size ?: 0}首）"
                }
            }
        )
        rv.adapter = playlistDialogAdapter

        playlistDialog?.setContentView(view)
        playlistDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val bottomSheet = playlistDialog?.findViewById<View>(MaterialR.id.design_bottom_sheet)
        bottomSheet?.let {
            it.setBackgroundResource(android.R.color.transparent)
            it.clipToOutline = true
        }

        playlistDialog?.show()
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        runOnUiThread {
            btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            adapter.setPlaying(isPlaying)

            currentIndex = musicBinder?.getCurrentIndex() ?: 0
            playList = musicBinder?.getPlayList() ?: emptyList()
            adapter.updateCurrentIndex(currentIndex)
            adapter.notifyDataSetChanged()
            updateUI()
        }
    }

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_PLAYBACK_STATE_CHANGED) {
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                btnPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
                adapter.setPlaying(isPlaying)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(playbackReceiver, IntentFilter(ACTION_PLAYBACK_STATE_CHANGED), Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(playbackReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateTimer?.cancel()
        if (isBound) {
            musicBinder?.setPlaybackStateListener(null)
            unbindService(connection)
        }
    }

    private fun formatTime(ms: Int): String = String.format("%d:%02d", ms / 60000, (ms / 1000) % 60)
}
