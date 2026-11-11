package com.example.iptv

data class Channel(val name: String, val url: String, val group: String = "")

object M3uParser {
    private val nameRe = Regex(",(.*)$")
    private val groupRe = Regex("group-title=\"([^\"]*)\"")

    fun parse(text: String): List<Channel> {
        val out = mutableListOf<Channel>()
        var name = ""; var group = ""
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    name = nameRe.find(line)?.groupValues?.get(1)?.trim().orEmpty()
                    group = groupRe.find(line)?.groupValues?.get(1).orEmpty()
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    out += Channel(name.ifEmpty { line }, line, group)
                    name = ""; group = ""
                }
            }
        }
        return out
    }
}
