package com.iditechs.filmu

object ContentFilter {

    // --- VOD (Movies) ---

    data class FilteredMovie(
        val name: String,
        val streamIds: List<Int>,
        val extensions: List<String>,
        val imagePath: String?,
        val rating: Double,
        val genres: List<String>,
        val isAdult: Boolean
    )

    fun filterMovies(
        streams: List<VodStream>,
        categories: List<Category>,
        excludeAdult: Boolean = true
    ): List<FilteredMovie> {
        val categoryMap = categories.associate { (it.category_id ?: "") to (it.category_name ?: "") }

        val movies = streams.map { stream ->
            val catName = categoryMap[stream.category_id] ?: ""
            val isAdult = ADULT_KEYWORDS.any { catName.contains(it, ignoreCase = true) }
            val genres = parseGenres(catName)
            val rating = stream.rating_5based ?: stream.rating?.toDoubleOrNull() ?: 0.0

            FilteredMovie(
                name = cleanTitle(stream.name ?: ""),
                streamIds = listOf(stream.stream_id),
                extensions = listOfNotNull(stream.container_extension),
                imagePath = stream.stream_icon?.takeIf { it.isNotBlank() },
                rating = rating,
                genres = genres,
                isAdult = isAdult
            )
        }

        val filtered = if (excludeAdult) movies.filter { !it.isAdult } else movies

        return deduplicateMovies(filtered)
    }

    private fun deduplicateMovies(movies: List<FilteredMovie>): List<FilteredMovie> {
        val groups = mutableMapOf<String, FilteredMovie>()

        for (movie in movies) {
            val key = normalizeForDedup(movie.name)
            val existing = groups[key]
            if (existing != null) {
                groups[key] = existing.copy(
                    streamIds = (existing.streamIds + movie.streamIds).distinct(),
                    extensions = (existing.extensions + movie.extensions).distinct(),
                    rating = maxOf(existing.rating, movie.rating),
                    genres = (existing.genres + movie.genres).distinct(),
                    imagePath = existing.imagePath ?: movie.imagePath
                )
            } else {
                groups[key] = movie
            }
        }

        return groups.values.toList()
    }

    // --- Series ---

    data class FilteredSeries(
        val name: String,
        val seriesIds: List<Int>,
        val cover: String?,
        val plot: String?,
        val rating: Double,
        val genres: List<String>,
        val releaseDate: String?
    )

    fun filterSeries(
        series: List<SeriesItem>,
        categories: List<Category>
    ): List<FilteredSeries> {
        val categoryMap = categories.associate { (it.category_id ?: "") to (it.category_name ?: "") }

        val items = series.map { s ->
            val catGenres = parseGenres(categoryMap[s.category_id] ?: "")
            val inlineGenres = s.genre?.let { parseGenres(it) } ?: emptyList()
            val allGenres = (catGenres + inlineGenres).distinct()
            val rating = s.rating_5based ?: s.rating?.toDoubleOrNull() ?: 0.0

            FilteredSeries(
                name = cleanTitle(s.name ?: ""),
                seriesIds = listOf(s.series_id),
                cover = s.cover?.takeIf { it.isNotBlank() },
                plot = s.plot?.takeIf { it.isNotBlank() },
                rating = rating,
                genres = allGenres,
                releaseDate = s.releaseDate
            )
        }

        return deduplicateSeries(items)
    }

    private fun deduplicateSeries(items: List<FilteredSeries>): List<FilteredSeries> {
        val groups = mutableMapOf<String, FilteredSeries>()

        for (s in items) {
            val key = normalizeForDedup(s.name)
            val existing = groups[key]
            if (existing != null) {
                groups[key] = existing.copy(
                    seriesIds = (existing.seriesIds + s.seriesIds).distinct(),
                    rating = maxOf(existing.rating, s.rating),
                    genres = (existing.genres + s.genres).distinct(),
                    cover = existing.cover ?: s.cover,
                    plot = existing.plot ?: s.plot,
                    releaseDate = existing.releaseDate ?: s.releaseDate
                )
            } else {
                groups[key] = s
            }
        }

        return groups.values.toList()
    }

    // --- Live Channels ---

    enum class Resolution(val label: String) {
        SD("SD"), HD("HD"), FHD("FHD"), UHD4K("4K")
    }

    enum class ChannelType {
        GENERAL, KID, MUSIC, CINEMA, SPORT, DOCUMENTARY, NEWS
    }

    data class FilteredChannel(
        val name: String,
        val streamIds: List<Int>,
        val icon: String?,
        val resolution: Resolution?,
        val country: String?,
        val language: String?,
        val type: ChannelType,
        val categoryId: String?,
        val epgChannelId: String?,
        val hasArchive: Boolean
    )

    fun filterChannels(
        streams: List<LiveStream>,
        categories: List<Category>,
        removeNoIcon: Boolean = false
    ): List<FilteredChannel> {
        val categoryMap = categories.associate { (it.category_id ?: "") to (it.category_name ?: "") }

        val channels = streams
            .filter { !it.name.isNullOrBlank() }
            .map { stream ->
                val rawName = stream.name ?: ""
                val catName = categoryMap[stream.category_id] ?: ""
                val resolution = extractResolution(rawName)
                val cleanedName = cleanChannelName(rawName)
                val (country, language) = extractCountryLanguage(catName)
                val type = categorizeChannel(catName)

                FilteredChannel(
                    name = cleanedName,
                    streamIds = listOf(stream.stream_id),
                    icon = stream.stream_icon?.takeIf { it.isNotBlank() },
                    resolution = resolution,
                    country = country,
                    language = language,
                    type = type,
                    categoryId = stream.category_id,
                    epgChannelId = stream.epg_channel_id,
                    hasArchive = (stream.tv_archive ?: 0) > 0
                )
            }

        val filtered = if (removeNoIcon) channels.filter { it.icon != null } else channels

        return deduplicateChannels(filtered)
    }

    private fun deduplicateChannels(channels: List<FilteredChannel>): List<FilteredChannel> {
        val groups = mutableMapOf<String, FilteredChannel>()

        for (ch in channels) {
            val key = "${normalizeForDedup(ch.name)}|${ch.resolution?.name ?: ""}"
            val existing = groups[key]
            if (existing != null) {
                groups[key] = existing.copy(
                    streamIds = (existing.streamIds + ch.streamIds).distinct(),
                    icon = existing.icon ?: ch.icon,
                    resolution = existing.resolution ?: ch.resolution,
                    country = existing.country ?: ch.country,
                    language = existing.language ?: ch.language
                )
            } else {
                groups[key] = ch
            }
        }

        return groups.values.toList()
    }

    // --- Utilities ---

    private val ADULT_KEYWORDS = listOf("adult", "xxx", "porn", "+18", "18+", "erotic")

    private val RESOLUTION_PATTERNS = listOf(
        Regex("""[\s|(]\s*4[Kk]\s*[)|]?""") to Resolution.UHD4K,
        Regex("""[\s|(]\s*[Ff][Hh][Dd]\s*[)|]?""") to Resolution.FHD,
        Regex("""[\s|(]\s*[Hh][Dd]\s*[)|]?""") to Resolution.HD,
        Regex("""[\s|(]\s*[Ss][Dd]\s*[)|]?""") to Resolution.SD,
        // Unicode variants from the old filter
        Regex("""[\s|(]\s*[ＨHℍ𝐇𝗛][ＤDⅮ𝐃𝗗]\s*[)|]?""") to Resolution.HD,
        Regex("""[\s|(]\s*[ＦFℱ𝐅𝗙][ＨHℍ𝐇𝗛][ＤDⅮ𝐃𝗗]\s*[)|]?""") to Resolution.FHD,
    )

    private fun extractResolution(name: String): Resolution? {
        for ((pattern, resolution) in RESOLUTION_PATTERNS) {
            if (pattern.containsMatchIn(name)) return resolution
        }
        return null
    }

    private fun cleanChannelName(name: String): String {
        var cleaned = name
        // Remove resolution tags
        for ((pattern, _) in RESOLUTION_PATTERNS) {
            cleaned = pattern.replace(cleaned, " ")
        }
        // Remove common prefixes like country codes "FR:", "UK |", etc.
        cleaned = cleaned.replace(Regex("""^[A-Z]{2,3}\s*[:|]\s*"""), "")
        // Remove pipe-separated suffixes (often port identifiers)
        cleaned = cleaned.replace(Regex("""\s*\|\s*\S+\s*$"""), "")
        // Collapse whitespace
        cleaned = cleaned.replace(Regex("""\s+"""), " ").trim()
        return cleaned
    }

    private val COUNTRY_MAP = mapOf(
        "france" to "FR", "french" to "FR", "français" to "FR", "francais" to "FR",
        "mali" to "ML", "malien" to "ML",
        "senegal" to "SN", "sénégal" to "SN",
        "ivory" to "CI", "ivoire" to "CI", "côte" to "CI", "cote" to "CI",
        "cameroon" to "CM", "cameroun" to "CM",
        "congo" to "CG", "gabon" to "GA", "guinea" to "GN", "guinée" to "GN",
        "togo" to "TG", "benin" to "BJ", "bénin" to "BJ",
        "burkina" to "BF", "niger" to "NE", "tchad" to "TD", "chad" to "TD",
        "morocco" to "MA", "maroc" to "MA", "tunisia" to "TN", "tunisie" to "TN",
        "algeria" to "DZ", "algérie" to "DZ", "algerie" to "DZ",
        "uk" to "GB", "united kingdom" to "GB", "england" to "GB",
        "usa" to "US", "united states" to "US", "america" to "US",
        "germany" to "DE", "allemagne" to "DE", "deutsch" to "DE",
        "spain" to "ES", "espagne" to "ES", "español" to "ES",
        "italy" to "IT", "italie" to "IT", "italiano" to "IT",
        "portugal" to "PT", "brazil" to "BR", "brésil" to "BR",
        "arab" to "AR", "arabic" to "AR", "arabe" to "AR",
        "turkey" to "TR", "turk" to "TR", "turc" to "TR",
        "india" to "IN", "hindi" to "IN", "bollywood" to "IN",
        "nigeria" to "NG", "ghana" to "GH", "kenya" to "KE",
        "south africa" to "ZA", "afrique du sud" to "ZA",
        "egypt" to "EG", "egypte" to "EG",
    )

    private val LANGUAGE_MAP = mapOf(
        "french" to "fr", "français" to "fr", "francais" to "fr", "france" to "fr",
        "english" to "en", "anglais" to "en",
        "arabic" to "ar", "arabe" to "ar", "arab" to "ar",
        "spanish" to "es", "espagnol" to "es", "español" to "es",
        "german" to "de", "allemand" to "de", "deutsch" to "de",
        "italian" to "it", "italien" to "it",
        "portuguese" to "pt", "portugais" to "pt",
        "turkish" to "tr", "turc" to "tr",
        "hindi" to "hi",
        "bambara" to "bm",
    )

    private val TYPE_KEYWORDS = mapOf(
        ChannelType.KID to listOf("kid", "enfant", "cartoon", "disney", "nickelodeon", "boing", "gulli"),
        ChannelType.MUSIC to listOf("music", "musique", "mtv", "trace"),
        ChannelType.CINEMA to listOf("cinema", "cinéma", "movie", "film", "hbo", "netflix", "amazon"),
        ChannelType.SPORT to listOf("sport", "football", "soccer", "bein", "espn", "dazn", "canal+ sport"),
        ChannelType.DOCUMENTARY to listOf("document", "discovery", "national geo", "nat geo", "histoire", "science"),
        ChannelType.NEWS to listOf("news", "info", "journal", "actualit", "cnbc", "bbc news", "cnn", "al jazeera"),
    )

    private fun extractCountryLanguage(categoryName: String): Pair<String?, String?> {
        val lower = categoryName.lowercase()
        var country: String? = null
        var language: String? = null

        for ((keyword, code) in COUNTRY_MAP) {
            if (lower.contains(keyword)) {
                country = code
                break
            }
        }
        for ((keyword, code) in LANGUAGE_MAP) {
            if (lower.contains(keyword)) {
                language = code
                break
            }
        }

        return country to language
    }

    private fun categorizeChannel(categoryName: String): ChannelType {
        val lower = categoryName.lowercase()
        for ((type, keywords) in TYPE_KEYWORDS) {
            if (keywords.any { lower.contains(it) }) return type
        }
        return ChannelType.GENERAL
    }

    private fun parseGenres(raw: String): List<String> {
        return raw.split(Regex("""[/&,|]"""))
            .map { it.trim() }
            .filter { it.isNotBlank() && it.length > 1 }
    }

    private fun cleanTitle(name: String): String {
        return name
            .replace(Regex("""\s*\([^)]*\)\s*$"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun normalizeForDedup(name: String): String {
        return name.lowercase()
            .replace(Regex("""[^a-z0-9\s]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
