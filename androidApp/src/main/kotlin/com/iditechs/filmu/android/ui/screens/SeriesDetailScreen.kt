package com.iditechs.filmu.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iditechs.filmu.SeriesEpisode
import com.iditechs.filmu.SeriesInfo
import com.iditechs.filmu.android.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: Int,
    viewModel: AppViewModel,
    onPlay: (url: String, title: String) -> Unit,
    onBack: () -> Unit,
) {
    var seriesInfo by remember { mutableStateOf<SeriesInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSeason by rememberSaveable { mutableStateOf<String?>(null) }
    var seasonExpanded by remember { mutableStateOf(false) }

    val seriesList by viewModel.seriesList.collectAsState()
    val seriesItem = remember(seriesList, seriesId) { seriesList.find { it.series_id == seriesId } }

    LaunchedEffect(seriesId) {
        isLoading = true
        seriesInfo = viewModel.getSeriesInfo(seriesId)
        val seasons = seriesInfo?.episodes?.keys?.sorted()
        if (selectedSeason == null && !seasons.isNullOrEmpty()) {
            selectedSeason = seasons.first()
        }
        isLoading = false
    }

    val title = seriesInfo?.info?.name ?: seriesItem?.name ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val info = seriesInfo
        val cover = info?.info?.cover ?: seriesItem?.cover
        val seasonKeys = info?.episodes?.keys?.sorted() ?: emptyList()
        val episodes = if (selectedSeason != null) {
            info?.episodes?.get(selectedSeason) ?: emptyList()
        } else {
            emptyList()
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            item {
                if (!cover.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                    ) {
                        AsyncImage(
                            model = cover,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF0A0A0A)),
                                        startY = 120f,
                                    )
                                ),
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    val rating = info?.info?.rating_5based ?: seriesItem?.rating_5based
                    if (rating != null && rating > 0) {
                        Text(
                            text = "★ ${"%.1f".format(rating)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    val plot = info?.info?.plot ?: seriesItem?.plot
                    if (!plot.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = plot,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val genre = info?.info?.genre ?: seriesItem?.genre
                    if (!genre.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (seasonKeys.isNotEmpty()) {
                        Box {
                            OutlinedButton(onClick = { seasonExpanded = true }) {
                                Text("Saison $selectedSeason")
                            }
                            DropdownMenu(
                                expanded = seasonExpanded,
                                onDismissRequest = { seasonExpanded = false },
                            ) {
                                seasonKeys.forEach { season ->
                                    DropdownMenuItem(
                                        text = { Text("Saison $season") },
                                        onClick = {
                                            selectedSeason = season
                                            seasonExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            items(episodes) { episode ->
                EpisodeRow(
                    episode = episode,
                    seriesName = title,
                    viewModel = viewModel,
                    onPlay = onPlay,
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: SeriesEpisode,
    seriesName: String,
    viewModel: AppViewModel,
    onPlay: (url: String, title: String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                val epId = episode.id
                val ext = episode.container_extension
                if (epId != null && ext != null) {
                    val url = viewModel.getSeriesStreamUrl(epId, ext)
                    if (url != null) {
                        val epNum = episode.episode_num ?: 0
                        val seasonNum = episode.season ?: 0
                        onPlay(url, "$seriesName S${seasonNum}E$epNum")
                    }
                }
            },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Épisode ${episode.episode_num ?: "?"}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                val epTitle = episode.title
                if (!epTitle.isNullOrBlank()) {
                    Text(
                        text = epTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val duration = episode.info?.duration
                if (!duration.isNullOrBlank()) {
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
