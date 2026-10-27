package com.iditechs.filmu.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.iditechs.filmu.android.ui.AppViewModel
import com.iditechs.filmu.android.ui.components.ContentCard
import kotlinx.coroutines.delay

@Composable
fun MoviesTab(
    viewModel: AppViewModel,
    onMovieClick: (streamId: Int, extension: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val categories by viewModel.vodCategories.collectAsState()
    val movies by viewModel.vodStreams.collectAsState()
    val searchResults by viewModel.vodSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val isSearchMode = searchQuery.length >= 2
    val displayList = if (isSearchMode) searchResults else movies

    LaunchedEffect(Unit) {
        if (categories.isEmpty()) viewModel.loadVodCategories()
    }

    LaunchedEffect(selectedCategoryId, categories) {
        if (!isSearchMode) {
            val catId = selectedCategoryId ?: categories.firstOrNull()?.category_id
            if (catId != null) viewModel.loadVodStreams(catId)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(800)
            viewModel.searchVod(searchQuery)
        } else {
            viewModel.clearSearch()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher un film...") },
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
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(displayList, key = { it.stream_id }) { movie ->
                        ContentCard(
                            imageUrl = movie.stream_icon,
                            title = movie.name ?: "",
                            rating = movie.rating_5based,
                            modifier = Modifier.clickable {
                                onMovieClick(movie.stream_id, movie.container_extension ?: "mp4")
                            },
                        )
                    }
                }

                if ((isLoading && !isSearchMode && movies.isEmpty()) || (isSearching)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
