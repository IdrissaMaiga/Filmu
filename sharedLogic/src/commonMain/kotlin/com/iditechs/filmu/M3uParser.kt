package com.iditechs.filmu

object M3uParser {

    data class M3uChannel(
        val name: String,
        val url: String,
        val logo: String? = null,
        val group: String? = null,
        val country: String? = null,
        val resolution: String? = null,
        val tvgId: String? = null,
        val tvgName: String? = null,
    )

    data class M3uPlaylist(
        val channels: List<M3uChannel>,
        val groups: Map<String, List<M3uChannel>>,
        val countries: Map<String, List<M3uChannel>>,
    )

    private val TAG_REGEX = Regex("""(\S+?)="([^"]*?)"""")

    private val RESOLUTION_MAP = mapOf(
        "ᴴᴰ" to "HD",
        "ˢᴰ" to "SD",
        "ᵁᴴᴰ" to "4K",
        "ᶠᴴᴰ" to "FHD",
        "4K" to "4K",
        "FHD" to "FHD",
        "HD" to "HD",
        "SD" to "SD",
        "UHD" to "4K",
        "1080" to "FHD",
        "1080p" to "FHD",
        "720" to "HD",
        "720p" to "HD",
        "480" to "SD",
        "480p" to "SD",
        "360p" to "SD",
    )

    private val COUNTRY_PATTERNS = listOf(
        Regex("""\|([A-Z]{2})\|"""),
        Regex("""\[([A-Z]{2})\]"""),
        Regex("""^([A-Z]{2})\s*[-:]"""),
    )

    fun parse(m3uContent: String): M3uPlaylist {
        val lines = m3uContent.lines()
        val channels = mutableListOf<M3uChannel>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val tags = parseTags(line)
                val displayName = extractDisplayName(line)
                val url = findNextUrl(lines, i + 1)

                if (url != null) {
                    val name = tags["tvg-name"] ?: displayName ?: "Unknown"
                    channels.add(
                        M3uChannel(
                            name = name,
                            url = url,
                            logo = tags["tvg-logo"],
                            group = tags["group-title"],
                            country = extractCountry(name, tags["group-title"]),
                            resolution = extractResolution(name),
                            tvgId = tags["tvg-id"],
                            tvgName = tags["tvg-name"],
                        )
                    )
                }
            }
            i++
        }

        val deduped = dedup(channels)
        val groups = deduped.groupBy { it.group ?: "Uncategorized" }
        val countries = deduped.filter { it.country != null }.groupBy { it.country!! }

        return M3uPlaylist(channels = deduped, groups = groups, countries = countries)
    }

    private fun parseTags(line: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        TAG_REGEX.findAll(line).forEach { match ->
            result[match.groupValues[1].lowercase()] = match.groupValues[2]
        }
        return result
    }

    private fun extractDisplayName(line: String): String? {
        val commaIdx = line.lastIndexOf(',')
        return if (commaIdx >= 0) line.substring(commaIdx + 1).trim().ifBlank { null } else null
    }

    private fun findNextUrl(lines: List<String>, startIdx: Int): String? {
        var j = startIdx
        while (j < lines.size) {
            val l = lines[j].trim()
            if (l.isNotBlank() && !l.startsWith("#")) return l
            if (l.startsWith("#EXTINF:")) return null
            j++
        }
        return null
    }

    private fun extractCountry(name: String, group: String?): String? {
        val text = "$name ${group ?: ""}"
        for (pattern in COUNTRY_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    private fun extractResolution(name: String): String? {
        for ((key, value) in RESOLUTION_MAP) {
            if (name.contains(key, ignoreCase = true)) return value
        }
        return null
    }

    private fun dedup(channels: List<M3uChannel>): List<M3uChannel> {
        val seen = mutableSetOf<String>()
        return channels.filter { ch ->
            val key = "${ch.name.lowercase().trim()}|${ch.url}"
            seen.add(key)
        }
    }
}
