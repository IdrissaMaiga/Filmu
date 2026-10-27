package com.iditechs.filmu.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iditechs.filmu.Category
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
            onDone()
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
                _liveStreams.value = c.getLiveStreams(categoryId)
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
                _vodStreams.value = c.getVodStreams(categoryId)
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
                _seriesList.value = c.getSeries(categoryId)
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
            movies = _vodStreams.value.filter { it.name?.lowercase()?.contains(q) == true },
            series = _seriesList.value.filter { it.name?.lowercase()?.contains(q) == true },
            live = _liveStreams.value.filter { it.name?.lowercase()?.contains(q) == true },
        )
    }

    fun clearError() {
        _error.value = null
    }
}

data class SearchResults(
    val movies: List<VodStream> = emptyList(),
    val series: List<SeriesItem> = emptyList(),
    val live: List<LiveStream> = emptyList(),
)
