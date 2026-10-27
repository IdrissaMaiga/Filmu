package com.iditechs.filmu

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerCredentials(
    val serverUrl: String,
    val username: String,
    val password: String
)

@Serializable
data class ServerInfo(
    val user_info: UserInfo? = null,
    val server_info: ServerInfoData? = null
)

@Serializable
data class UserInfo(
    val username: String? = null,
    val password: String? = null,
    val status: String? = null,
    val exp_date: String? = null,
    val is_trial: String? = null,
    val active_cons: String? = null,
    val created_at: String? = null,
    val max_connections: String? = null,
    val allowed_output_formats: List<String> = emptyList()
)

@Serializable
data class ServerInfoData(
    val url: String? = null,
    val port: String? = null,
    val https_port: String? = null,
    val server_protocol: String? = null,
    val rtmp_port: String? = null,
    val timezone: String? = null,
    val timestamp_now: Long? = null,
    val time_now: String? = null
)

@Serializable
data class Category(
    val category_id: String? = null,
    val category_name: String? = null,
    val parent_id: Int = 0
)

@Serializable
data class LiveStream(
    val num: Int? = null,
    val name: String? = null,
    val stream_type: String? = null,
    val stream_id: Int = 0,
    val stream_icon: String? = null,
    val epg_channel_id: String? = null,
    val added: String? = null,
    val category_id: String? = null,
    val custom_sid: String? = null,
    val tv_archive: Int? = null,
    val direct_source: String? = null,
    val tv_archive_duration: Int? = null
)

@Serializable
data class VodStream(
    val num: Int? = null,
    val name: String? = null,
    val stream_type: String? = null,
    val stream_id: Int = 0,
    val stream_icon: String? = null,
    val rating: String? = null,
    val rating_5based: Double? = null,
    val added: String? = null,
    val category_id: String? = null,
    val container_extension: String? = null,
    val custom_sid: String? = null,
    val direct_source: String? = null
)

@Serializable
data class SeriesItem(
    val num: Int? = null,
    val name: String? = null,
    val series_id: Int = 0,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    @SerialName("releaseDate")
    val releaseDate: String? = null,
    val last_modified: String? = null,
    val rating: String? = null,
    val rating_5based: Double? = null,
    val backdrop_path: List<String> = emptyList(),
    val youtube_trailer: String? = null,
    val episode_run_time: String? = null,
    val category_id: String? = null
)

@Serializable
data class SeriesInfo(
    val seasons: List<SeriesSeason> = emptyList(),
    val info: SeriesItem? = null,
    val episodes: Map<String, List<SeriesEpisode>> = emptyMap()
)

@Serializable
data class SeriesSeason(
    val air_date: String? = null,
    val episode_count: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    val season_number: Int? = null,
    val cover: String? = null,
    val cover_big: String? = null
)

@Serializable
data class SeriesEpisode(
    val id: String? = null,
    val episode_num: Int? = null,
    val title: String? = null,
    val container_extension: String? = null,
    val info: EpisodeInfo? = null,
    val custom_sid: String? = null,
    val added: String? = null,
    val season: Int? = null,
    val direct_source: String? = null
)

@Serializable
data class EpisodeInfo(
    val movie_image: String? = null,
    val plot: String? = null,
    val releasedate: String? = null,
    val rating: Double? = null,
    val duration_secs: Int? = null,
    val duration: String? = null,
    val bitrate: Int? = null
)

@Serializable
data class EpgListing(
    val epg_listings: List<EpgEntry> = emptyList()
)

@Serializable
data class EpgEntry(
    val id: String? = null,
    val epg_id: String? = null,
    val title: String? = null,
    val lang: String? = null,
    val start: String? = null,
    val end: String? = null,
    val description: String? = null,
    val channel_id: String? = null,
    val start_timestamp: String? = null,
    val stop_timestamp: String? = null
)
