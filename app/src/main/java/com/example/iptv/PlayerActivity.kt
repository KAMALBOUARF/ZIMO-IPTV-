package com.example.iptv

import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var index = 0
    private val list get() = MainActivity.playing

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_player)
        index = intent.getIntExtra("index", 0)
        player = ExoPlayer.Builder(this).build().also {
            findViewById<PlayerView>(R.id.player).player = it
        }
        play()
    }

    private fun play() {
        if (list.isEmpty()) return
        index = (index + list.size) % list.size
        findViewById<TextView>(R.id.title).text = list[index].name
        player?.apply { setMediaItem(MediaItem.fromUri(list[index].url)); prepare(); playWhenReady = true }
    }

    // Télécommande Android TV : haut/bas ou CH+/CH- pour changer de chaîne
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> { index++; play(); true }
        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> { index--; play(); true }
        else -> super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() { player?.release(); super.onDestroy() }
}
