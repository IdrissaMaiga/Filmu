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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.delay

@Composable
fun ChannelsTab(
    viewModel: AppViewModel,
    onChannelClick: (streamId: Int) -> Unit,
    onPlayLive: (url: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val categories by viewModel.liveCategories.collectAsState()
    val streams by viewModel.liveStreams.collectAsState()
    val searchResults by viewModel.liveSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isSearchMode = searchQuery.length >= 2
    val displayList = if (isSearchMode) searchResults else streams

    LaunchedEffect(Unit) {
        if (categories.isEmpty()) viewModel.loadLiveCategories()
    }

    LaunchedEffect(selectedCategoryId, categories) {
        if (!isSearchMode) {
            val catId = selectedCategoryId ?: categories.firstOrNull()?.category_id
            if (catId != null) viewModel.loadLiveStreams(catId)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(800)
            viewModel.searchLive(searchQuery)
        } else {
            viewModel.clearSearch()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une chaîne...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )

        if (!isSearchMode && categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.category_id ||
                                (selectedCategoryId == null && cat == categories.first()),
                        onClick = { selectedCategoryId = cat.category_id },
                        label = { Text(cat.category_name ?: "") },
                    )
                }
            }
        }

        if (isSearchMode && !isSearching && searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun résultat", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(displayList, key = { it.stream_id }) { stream ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChannelClick(stream.stream_id) }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val icon = stream.stream_icon
                                if (!icon.isNullOrBlank()) {
                                    AsyncImage(
                                        model = icon,
                                        contentDescription = stream.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Text(
                                    text = stream.name ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = {
                                    val url = viewModel.getLiveStreamUrl(stream.stream_id)
                                    if (url != null) onPlayLive(url, stream.name ?: "Live")
                                }) {
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

                if ((isLoading && !isSearchMode && streams.isEmpty()) || (isSearching)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
