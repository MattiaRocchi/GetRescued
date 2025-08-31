package com.example.myapplication.utils

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val ACTION_START = "com.example.myapplication.action.START_MUSIC"
        const val ACTION_STOP = "com.example.myapplication.action.STOP_MUSIC"
        const val ACTION_SET_VOLUME = "com.example.myapplication.action.SET_VOLUME"
        const val EXTRA_VOLUME = "extra_volume"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_START, null -> {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, com.example.myapplication.R.raw.background_music)
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.setVolume(0.5f, 0.5f)
                }
                // if the intent contains a volume, set it
                val vol = intent?.getFloatExtra(EXTRA_VOLUME, -1f) ?: -1f
                if (vol >= 0f) {
                    setPlayerVolume(vol)
                }
                mediaPlayer?.start()
            }

            ACTION_SET_VOLUME -> {
                val vol = intent.getFloatExtra(EXTRA_VOLUME, 0.5f)
                setPlayerVolume(vol)
            }

            ACTION_STOP -> {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                stopSelf()
            }

            else -> {
                // unknown action -> safe: start if not running
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(this, com.example.myapplication.R.raw.background_music)
                    mediaPlayer?.isLooping = true
                    mediaPlayer?.setVolume(0.5f, 0.5f)
                }
                mediaPlayer?.start()
            }
        }

        return START_STICKY
    }

    private fun setPlayerVolume(vol: Float) {
        val v = when {
            vol < 0f -> 0f
            vol > 1f -> 1f
            else -> vol
        }
        mediaPlayer?.setVolume(v, v)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
