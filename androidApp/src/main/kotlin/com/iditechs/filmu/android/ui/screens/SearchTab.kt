package com.iditechs.filmu.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iditechs.filmu.android.ui.AppViewModel
import com.iditechs.filmu.android.ui.SearchResults
import com.iditechs.filmu.android.ui.components.ContentCard

@Composable
fun SearchTab(
    viewModel: AppViewModel,
    onMovieClick: (streamId: Int, extension: String) -> Unit,
    onSeriesClick: (seriesId: Int) -> Unit,
    onPlayLive: (url: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by rememberSaveable { mutableStateOf(SearchResults()) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                results = if (newQuery.length >= 2) viewModel.search(newQuery) else SearchResults()
            },
            placeholder = { Text("Rechercher films, séries, chaînes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val hasResults = results.movies.isNotEmpty() || results.series.isNotEmpty() || results.channels.isNotEmpty()

            if (!hasResults && query.length >= 2) {
                Text(
                    text = "Aucun résultat",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (results.movies.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Films (${results.movies.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        )
                    }
                    items(results.movies, key = { "vod_${it.streamIds.first()}" }) { movie ->
                        ContentCard(
                            imageUrl = movie.imagePath,
                            title = movie.name,
                            rating = movie.rating,
                            modifier = Modifier.clickable {
                                onMovieClick(movie.streamIds.first(), movie.extensions.firstOrNull() ?: "mp4")
                            },
                        )
                    }
                }

                if (results.series.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Séries (${results.series.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        )
                    }
                    items(results.series, key = { "ser_${it.seriesIds.first()}" }) { item ->
                        ContentCard(
                            imageUrl = item.cover,
                            title = item.name,
                            rating = item.rating,
                            modifier = Modifier.clickable { onSeriesClick(item.seriesIds.first()) },
                        )
                    }
                }

                if (results.channels.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Chaînes (${results.channels.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        )
                    }
                    items(results.channels, key = { "live_${it.streamIds.first()}" }) { channel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val url = viewModel.getLiveStreamUrl(channel.streamIds.first())
                                    if (url != null) onPlayLive(url, channel.name)
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val icon = channel.icon
                                if (!icon.isNullOrBlank()) {
                                    AsyncImage(
                                        model = icon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Fit,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
