package com.iditechs.filmu.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iditechs.filmu.Category
import com.iditechs.filmu.ContentFilter
import com.iditechs.filmu.CredentialStorage
import com.iditechs.filmu.FilmuClient
import com.iditechs.filmu.LiveStream
import com.iditechs.filmu.SeriesInfo
import com.iditechs.filmu.SeriesItem
import com.iditechs.filmu.ServerCredentials
import com.iditechs.filmu.ServerInfo
import com.iditechs.filmu.VodStream
import com.iditechs.filmu.android.FilmuApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FilmuApplication
    private val credentialStorage: CredentialStorage = app.credentialStorage

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _serverInfo = MutableStateFlow<ServerInfo?>(null)
    val serverInfo: StateFlow<ServerInfo?> = _serverInfo.asStateFlow()

    private val _liveCategories = MutableStateFlow<List<Category>>(emptyList())
    val liveCategories: StateFlow<List<Category>> = _liveCategories.asStateFlow()

    private val _vodCategories = MutableStateFlow<List<Category>>(emptyList())
    val vodCategories: StateFlow<List<Category>> = _vodCategories.asStateFlow()

    private val _seriesCategories = MutableStateFlow<List<Category>>(emptyList())
    val seriesCategories: StateFlow<List<Category>> = _seriesCategories.asStateFlow()

    private val _liveStreams = MutableStateFlow<List<LiveStream>>(emptyList())
    val liveStreams: StateFlow<List<LiveStream>> = _liveStreams.asStateFlow()

    private val _vodStreams = MutableStateFlow<List<VodStream>>(emptyList())
    val vodStreams: StateFlow<List<VodStream>> = _vodStreams.asStateFlow()

    private val _seriesList = MutableStateFlow<List<SeriesItem>>(emptyList())
    val seriesList: StateFlow<List<SeriesItem>> = _seriesList.asStateFlow()

    private val _excludeAdult = MutableStateFlow(true)
    val excludeAdult: StateFlow<Boolean> = _excludeAdult.asStateFlow()

    private val _filteredMovies = MutableStateFlow<List<ContentFilter.FilteredMovie>>(emptyList())
    val filteredMovies: StateFlow<List<ContentFilter.FilteredMovie>> = _filteredMovies.asStateFlow()

    private val _filteredSeries = MutableStateFlow<List<ContentFilter.FilteredSeries>>(emptyList())
    val filteredSeries: StateFlow<List<ContentFilter.FilteredSeries>> = _filteredSeries.asStateFlow()

    private val _filteredChannels = MutableStateFlow<List<ContentFilter.FilteredChannel>>(emptyList())
    val filteredChannels: StateFlow<List<ContentFilter.FilteredChannel>> = _filteredChannels.asStateFlow()

    val client: FilmuClient? get() = app.client

    init {
        viewModelScope.launch {
            val saved = credentialStorage.getCredentials()
            if (saved != null) {
                try {
                    val c = FilmuClient(saved)
                    val info = c.authenticate()
                    if (info.user_info?.status == "Active") {
                        app.client = c
                        _serverInfo.value = info
                        _isLoggedIn.value = true
                    } else {
                        c.close()
                        credentialStorage.clear()
                    }
                } catch (_: Exception) {
                    credentialStorage.clear()
                }
            }
        }
    }

    fun login(serverUrl: String, username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val creds = ServerCredentials(serverUrl.trimEnd('/'), username, password)
                val c = FilmuClient(creds)
                val info = c.authenticate()

                val status = info.user_info?.status
                if (status == "Active" || status == "Trial") {
                    app.client = c
                    credentialStorage.setCredentials(creds)
                    _serverInfo.value = info
                    _isLoggedIn.value = true
                    onSuccess()
                } else if (status == "Expired") {
                    c.close()
                    _error.value = "Abonnement expiré. Veuillez le renouveler."
                } else if (status == "Banned") {
                    c.close()
                    _error.value = "Compte suspendu."
                } else {
                    c.close()
                    _error.value = "Identifiants incorrects."
                }
            } catch (e: Exception) {
                _error.value = "Connexion impossible. Vérifiez l'URL et vos identifiants."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            app.client?.close()
            app.client = null
            credentialStorage.clear()
            _isLoggedIn.value = false
            _serverInfo.value = null
            _liveCategories.value = emptyList()
            _vodCategories.value = emptyList()
            _seriesCategories.value = emptyList()
            _liveStreams.value = emptyList()
            _vodStreams.value = emptyList()
            _seriesList.value = emptyList()
            _filteredMovies.value = emptyList()
            _filteredSeries.value = emptyList()
            _filteredChannels.value = emptyList()
            onDone()
        }
    }

    fun toggleAdultFilter() {
        _excludeAdult.value = !_excludeAdult.value
        refilter()
    }

    private fun refilter() {
        val vodCats = _vodCategories.value
        val serCats = _seriesCategories.value
        val liveCats = _liveCategories.value
        val rawVod = _vodStreams.value
        val rawSeries = _seriesList.value
        val rawLive = _liveStreams.value
        if (rawVod.isEmpty() && rawSeries.isEmpty() && rawLive.isEmpty()) return

        _filteredMovies.value = ContentFilter.filterMovies(rawVod, vodCats, excludeAdult = _excludeAdult.value)
        _filteredSeries.value = ContentFilter.filterSeries(rawSeries, serCats)
        _filteredChannels.value = ContentFilter.filterChannels(rawLive, liveCats)
    }

    fun loadAllContent() {
        val c = client ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val liveCats = c.getLiveCategories()
                val vodCats = c.getVodCategories()
                val serCats = c.getSeriesCategories()
                _liveCategories.value = liveCats
                _vodCategories.value = vodCats
                _seriesCategories.value = serCats

                val rawLive = c.getLiveStreams()
                val rawVod = c.getVodStreams()
                val rawSeries = c.getSeries()
                _liveStreams.value = rawLive
                _vodStreams.value = rawVod
                _seriesList.value = rawSeries

                _filteredMovies.value = ContentFilter.filterMovies(rawVod, vodCats, excludeAdult = _excludeAdult.value)
                _filteredSeries.value = ContentFilter.filterSeries(rawSeries, serCats)
                _filteredChannels.value = ContentFilter.filterChannels(rawLive, liveCats)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLiveCategories() {
        val c = client ?: return
        viewModelScope.launch {
            try {
                _liveCategories.value = c.getLiveCategories()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadVodCategories() {
        val c = client ?: return
        viewModelScope.launch {
            try {
                _vodCategories.value = c.getVodCategories()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadSeriesCategories() {
        val c = client ?: return
        viewModelScope.launch {
            try {
                _seriesCategories.value = c.getSeriesCategories()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadLiveStreams(categoryId: String? = null) {
        val c = client ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cats = _liveCategories.value.ifEmpty { c.getLiveCategories().also { _liveCategories.value = it } }
                val streams = c.getLiveStreams(categoryId)
                _liveStreams.value = streams
                if (categoryId == null) {
                    _filteredChannels.value = ContentFilter.filterChannels(streams, cats)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadVodStreams(categoryId: String? = null) {
        val c = client ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cats = _vodCategories.value.ifEmpty { c.getVodCategories().also { _vodCategories.value = it } }
                val streams = c.getVodStreams(categoryId)
                _vodStreams.value = streams
                if (categoryId == null) {
                    _filteredMovies.value = ContentFilter.filterMovies(streams, cats)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSeries(categoryId: String? = null) {
        val c = client ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cats = _seriesCategories.value.ifEmpty { c.getSeriesCategories().also { _seriesCategories.value = it } }
                val series = c.getSeries(categoryId)
                _seriesList.value = series
                if (categoryId == null) {
                    _filteredSeries.value = ContentFilter.filterSeries(series, cats)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getSeriesInfo(seriesId: Int): SeriesInfo? {
        val c = client ?: return null
        return try {
            c.getSeriesInfo(seriesId)
        } catch (e: Exception) {
            _error.value = e.message
            null
        }
    }

    suspend fun getEpg(streamId: Int): List<com.iditechs.filmu.EpgEntry> {
        val c = client ?: return emptyList()
        return try {
            c.getEpg(streamId)
        } catch (e: Exception) {
            _error.value = e.message
            emptyList()
        }
    }

    fun getLiveStreamUrl(streamId: Int): String? = client?.getLiveStreamUrl(streamId)
    fun getVodStreamUrl(streamId: Int, extension: String): String? = client?.getVodStreamUrl(streamId, extension)
    fun getSeriesStreamUrl(episodeId: String, extension: String): String? = client?.getSeriesStreamUrl(episodeId, extension)

    fun search(query: String): SearchResults {
        val q = query.lowercase()
        return SearchResults(
            movies = _filteredMovies.value.filter { it.name.lowercase().contains(q) },
            series = _filteredSeries.value.filter { it.name.lowercase().contains(q) },
            channels = _filteredChannels.value.filter { it.name.lowercase().contains(q) },
        )
    }

    fun clearError() {
        _error.value = null
    }
}

data class SearchResults(
    val movies: List<ContentFilter.FilteredMovie> = emptyList(),
    val series: List<ContentFilter.FilteredSeries> = emptyList(),
    val channels: List<ContentFilter.FilteredChannel> = emptyList(),
)
