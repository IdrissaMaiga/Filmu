package com.iditechs.filmu

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class FilmuClient(
    private val credentials: ServerCredentials
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val http = HttpClient {
        install(ContentNegotiation) { json(this@FilmuClient.json) }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
    }

    private val baseApi: String
        get() {
            val url = credentials.serverUrl.trimEnd('/')
            return "$url/player_api.php?username=${credentials.username}&password=${credentials.password}"
        }

    private fun streamBase(): String = credentials.serverUrl.trimEnd('/')

    // -- Auth / server info --

    suspend fun authenticate(): ServerInfo =
        http.get(baseApi).body()

    // -- Live TV --

    suspend fun getLiveCategories(): List<Category> =
        http.get("$baseApi&action=get_live_categories").body()

    suspend fun getLiveStreams(categoryId: String? = null): List<LiveStream> {
        val url = buildString {
            append("$baseApi&action=get_live_streams")
            categoryId?.let { append("&category_id=$it") }
        }
        return http.get(url).body()
    }

    // -- VOD (Movies) --

    suspend fun getVodCategories(): List<Category> =
        http.get("$baseApi&action=get_vod_categories").body()

    suspend fun getVodStreams(categoryId: String? = null): List<VodStream> {
        val url = buildString {
            append("$baseApi&action=get_vod_streams")
            categoryId?.let { append("&category_id=$it") }
        }
        return http.get(url).body()
    }

    // -- Series --

    suspend fun getSeriesCategories(): List<Category> =
        http.get("$baseApi&action=get_series_categories").body()

    suspend fun getSeries(categoryId: String? = null): List<SeriesItem> {
        val url = buildString {
            append("$baseApi&action=get_series")
            categoryId?.let { append("&category_id=$it") }
        }
        return http.get(url).body()
    }

    suspend fun getSeriesInfo(seriesId: Int): SeriesInfo =
        http.get("$baseApi&action=get_series_info&series_id=$seriesId").body()

    // -- EPG --

    suspend fun getEpg(streamId: Int): List<EpgEntry> {
        val listing: EpgListing = http.get("$baseApi&action=get_short_epg&stream_id=$streamId").body()
        return listing.epg_listings
    }

    // -- Stream URL builders (no network call) --

    fun getLiveStreamUrl(streamId: Int): String =
        "${streamBase()}/live/${credentials.username}/${credentials.password}/$streamId.ts"

    fun getVodStreamUrl(streamId: Int, extension: String): String =
        "${streamBase()}/movie/${credentials.username}/${credentials.password}/$streamId.$extension"

    fun getSeriesStreamUrl(episodeId: String, extension: String): String =
        "${streamBase()}/series/${credentials.username}/${credentials.password}/$episodeId.$extension"

    // -- Lifecycle --

    fun close() {
        http.close()
    }
}
