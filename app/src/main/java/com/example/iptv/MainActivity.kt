package com.example.iptv

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        var channels: List<Channel> = emptyList()
        var playing: List<Channel> = emptyList()
    }
    private var shown: List<Channel> = emptyList()

    private fun filter(all: List<Channel>, section: String): List<Channel> {
        val key = when (section) { "movies" -> listOf("movie", "film", "vod"); "series" -> listOf("serie", "série", "series"); else -> return all }
        return all.filter { c -> key.any { c.group.contains(it, true) } }
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("iptv", MODE_PRIVATE)
        val input = findViewById<EditText>(R.id.urlInput)
        val status = findViewById<TextView>(R.id.status)
        val list = findViewById<ListView>(R.id.list)
        input.setText(prefs.getString("url", ""))

        list.setOnItemClickListener { _, _, pos, _ ->
            playing = shown
            startActivity(Intent(this, PlayerActivity::class.java).putExtra("index", pos))
        }
        findViewById<Button>(R.id.loadBtn).setOnClickListener {
            val url = input.text.toString().trim()
            prefs.edit().putString("url", url).apply()
            status.text = "Chargement..."
            thread {
                try {
                    val text = URL(url).openStream().bufferedReader().use { it.readText() }
                    val parsed = M3uParser.parse(text)
                    runOnUiThread {
                        channels = parsed
                        shown = filter(parsed, intent.getStringExtra("section") ?: "live")
                        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, shown.map { it.name })
                        status.text = "${shown.size} chaînes"
                    }
                } catch (e: Exception) {
                    runOnUiThread { status.text = "Erreur: ${e.message}" }
                }
            }
        }
    }
}
