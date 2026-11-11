package com.example.iptv

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        setContentView(R.layout.activity_home)

        fun open(section: String) = startActivity(
            Intent(this, MainActivity::class.java).putExtra("section", section))

        mapOf(
            R.id.navLive to "live", R.id.cardLive to "live",
            R.id.navMovies to "movies", R.id.cardMovies to "movies",
            R.id.navSeries to "series", R.id.cardSeries to "series",
            R.id.navSettings to "settings", R.id.navFav to "live"
        ).forEach { (id, sec) -> findViewById<View>(id).setOnClickListener { open(sec) } }
        findViewById<View>(R.id.hero).setOnClickListener { open("live") }
        findViewById<View>(R.id.navHome).requestFocus()
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.channels.isNotEmpty()) { showFeatured(); return }
        val url = getSharedPreferences("iptv", MODE_PRIVATE).getString("url", "").orEmpty()
        val status = findViewById<TextView>(R.id.status)
        if (url.isBlank()) { status.text = "Ajoutez votre serveur dans Paramètres"; return }
        status.text = "Chargement..."
        thread {
            try {
                val parsed = M3uParser.parse(URL(url).openStream().bufferedReader().use { it.readText() })
                runOnUiThread { MainActivity.channels = parsed; status.text = "${parsed.size} chaînes"; showFeatured() }
            } catch (e: Exception) { runOnUiThread { status.text = "Erreur serveur" } }
        }
    }

    private fun showFeatured() {
        val row = findViewById<LinearLayout>(R.id.featured)
        row.removeAllViews()
        MainActivity.channels.take(12).forEachIndexed { i, ch ->
            val tile = TextView(this).apply {
                text = ch.name; gravity = Gravity.CENTER; textSize = 12f
                setTextColor(0xFF0B1430.toInt()); setBackgroundResource(R.drawable.tile_bg)
                isFocusable = true; isClickable = true; maxLines = 2
                layoutParams = LinearLayout.LayoutParams(120.dp, ViewGroup.LayoutParams.MATCH_PARENT).apply { marginEnd = 8.dp }
                setOnClickListener {
                    MainActivity.playing = MainActivity.channels
                    startActivity(Intent(this@HomeActivity, PlayerActivity::class.java).putExtra("index", i))
                }
            }
            row.addView(tile)
        }
    }

    private val Int.dp get() = (this * resources.displayMetrics.density).toInt()
}
