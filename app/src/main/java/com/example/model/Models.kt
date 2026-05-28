package com.example.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val username: String,
    val password: String,
    val message: String,
    val auth: Int,
    val status: String,
    val exp_date: String?,
    val is_trial: String?,
    val active_cons: String?,
    val created_at: String?,
    val max_connections: String?
)

data class ServerInfo(
    val url: String,
    val port: String,
    val https_port: String,
    val server_protocol: String,
    val rtmp_port: String,
    val timestamp_now: Long,
    val time_now: String,
    val timezone: String
)

data class LoginResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
)

data class Category(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int?
)

data class LiveStream(
    val num: Int?,
    val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int?,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int?
)

data class VodStream(
    val num: Int?,
    val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    val custom_sid: String?,
    val direct_source: String?
)

data class SeriesStream(
    val num: Int?,
    val name: String,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("cover") val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class SeriesInfoResponse(
    val info: SeriesInfoDetail?,
    val episodes: Map<String, List<Episode>>?
)

data class SeriesInfoDetail(
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?
)

data class Episode(
    val id: String,
    @SerializedName("episode_num") val episodeNum: String?,
    val title: String,
    @SerializedName("container_extension") val containerExtension: String?,
    val season: Int?,
    val info: EpisodeInfo?
)

data class EpisodeInfo(
    @SerializedName("movie_image") val movieImage: String?,
    val plot: String?,
    val duration: String?
)
