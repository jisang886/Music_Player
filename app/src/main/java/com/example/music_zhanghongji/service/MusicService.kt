package com.example.music_zhanghongji.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.music_zhanghongji.model.MusicInfo
import com.example.music_zhanghongji.model.PlayMode
import java.io.IOException
import kotlin.random.Random


const val ACTION_PLAYBACK_STATE_CHANGED = "com.example.music_zhanghongji.PLAYBACK_STATE_CHANGED"
const val EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING"

class MusicService : Service() {

    private var playMode = PlayMode.ORDER

    private val playList = mutableListOf<MusicInfo>()
    private var currentIndex = 0
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    interface PlaybackStateListener {
        fun onPlaybackStateChanged(isPlaying: Boolean)
    }

    private var playbackStateListener: PlaybackStateListener? = null



    private val binder = MusicBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    private fun notifyPlaybackStateChanged(isPlaying: Boolean) {
        playbackStateListener?.onPlaybackStateChanged(isPlaying)
        val intent = Intent(ACTION_PLAYBACK_STATE_CHANGED)
        intent.putExtra(EXTRA_IS_PLAYING, isPlaying)
        sendBroadcast(intent)
    }

    inner class MusicBinder : Binder() {

        fun getPlayList(): List<MusicInfo> = playList.toList()

        fun getCurrentIndex(): Int = currentIndex

        fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

        fun getDuration(): Int {
            return try {
                if (mediaPlayer != null && mediaPlayer!!.isPlaying || isPrepared) {
                    mediaPlayer!!.duration
                } else {
                    0
                }
            } catch (e: IllegalStateException) {
                0
            }
        }


        fun setPlaybackStateListener(listener: PlaybackStateListener?) {
            playbackStateListener = listener
        }

        fun getCurrentPosition(): Int {
            return try {
                if (mediaPlayer != null && isPrepared) {
                    mediaPlayer!!.currentPosition
                } else {
                    0
                }
            } catch (e: IllegalStateException) {
                0
            }
        }


        fun play() {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                notifyPlaybackStateChanged(true)
            }
        }

        fun pause() {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer?.pause()
                notifyPlaybackStateChanged(false)
            }
        }

        fun seekTo(pos: Int) {
            try {
                mediaPlayer?.seekTo(pos)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        fun removeAt(index: Int) {
            if (index !in playList.indices) return

            val removingCurrent = (index == currentIndex)
            playList.removeAt(index)

            if (playList.isEmpty()) {
                currentIndex = 0
                mediaPlayer?.stop()
                notifyPlaybackStateChanged(false)
                return
            }

            if (removingCurrent) {
                currentIndex = when (playMode) {
                    PlayMode.ORDER, PlayMode.SINGLE_LOOP -> if (index >= playList.size) 0 else index
                    PlayMode.RANDOM -> Random.nextInt(playList.size)
                }
                playAt(currentIndex)
            } else if (index < currentIndex) {
                currentIndex--
            }
        }

        private fun getRandomIndexExcept(current: Int, size: Int): Int {
            if (size <= 1) return current
            var next: Int
            do {
                next = Random.nextInt(size)
            } while (next == current)
            return next
        }


        fun playNext() {
            if (playList.isEmpty()) return

            currentIndex = when (playMode) {
                PlayMode.ORDER -> (currentIndex + 1) % playList.size
                PlayMode.RANDOM -> getRandomIndexExcept(currentIndex, playList.size)  // 随机切歌，排除当前索引
                PlayMode.SINGLE_LOOP -> currentIndex
            }
            playAt(currentIndex)
        }

        fun playPrevious() {
            if (playList.isEmpty()) return

            currentIndex = when (playMode) {
                PlayMode.ORDER -> if (currentIndex == 0) playList.size - 1 else currentIndex - 1
                PlayMode.RANDOM -> getRandomIndexExcept(currentIndex, playList.size)  // 随机回退，排除当前索引
                PlayMode.SINGLE_LOOP -> currentIndex
            }
            playAt(currentIndex)
        }




        fun playAt(index: Int) {
            if (playList.isEmpty()) return
            val newIndex = index.coerceIn(playList.indices)

            currentIndex = newIndex
            val music = playList[currentIndex]

            // 停止当前播放，防止状态混乱
            mediaPlayer?.reset()
            isPrepared = false

            prepareAndPlay(music)  // ✅ 这里会在准备好后自动 start
        }


        fun setPlayListAndPlay(list: List<MusicInfo>, startIndex: Int) {
            playList.clear()
            playList.addAll(list)
            currentIndex = startIndex.coerceIn(playList.indices)
            playAt(currentIndex)
        }

        fun playSingleMusic(music: MusicInfo) {
            val idx = playList.indexOfFirst { it.id == music.id }

            if (idx == -1) {
                playList.add(0, music)
                currentIndex = 0
            } else {
                currentIndex = idx
            }

            // 强制重新播放当前 index 的歌曲
            playAt(currentIndex)
        }



        fun addToPlayList(music: MusicInfo) {
            if (playList.none { it.id == music.id }) {
                playList.add(music)
            }
        }

        fun setPlayMode(mode: PlayMode) {
            playMode = mode
        }

        fun getPlayMode(): PlayMode = playMode

        private fun prepareAndPlay(music: MusicInfo) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            } else {
                mediaPlayer?.reset()
            }

            isPrepared = false

            try {
                mediaPlayer?.setDataSource(music.musicUrl)
                mediaPlayer?.prepareAsync()

                mediaPlayer?.setOnPreparedListener {
                    isPrepared = true
                    it.start()  // ✅ 关键：在准备好后立即播放
                    notifyPlaybackStateChanged(true)
                }

                mediaPlayer?.setOnCompletionListener {
                    isPrepared = false
                    if (playList.isEmpty()) {
                        notifyPlaybackStateChanged(false)
                        return@setOnCompletionListener
                    }

                    currentIndex = when (playMode) {
                        PlayMode.ORDER -> (currentIndex + 1) % playList.size
                        PlayMode.RANDOM -> {
                            if (playList.size <= 1) currentIndex
                            else {
                                var nextIndex: Int
                                do {
                                    nextIndex = Random.nextInt(playList.size)
                                } while (nextIndex == currentIndex)
                                nextIndex
                            }
                        }
                        PlayMode.SINGLE_LOOP -> currentIndex
                    }

                    playAt(currentIndex)
                }


                mediaPlayer?.setOnErrorListener { _, _, _ ->
                    isPrepared = false
                    notifyPlaybackStateChanged(false)
                    true
                }

            } catch (e: IOException) {
                isPrepared = false
                e.printStackTrace()
                notifyPlaybackStateChanged(false)
            }
        }

    }
}