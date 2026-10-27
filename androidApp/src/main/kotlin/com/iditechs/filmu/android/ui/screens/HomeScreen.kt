package com.iditechs.filmu.android.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.iditechs.filmu.android.ui.AppViewModel

private data class TabItem(val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem("Films", Icons.Default.Movie),
    TabItem("Séries", Icons.Default.Tv),
    TabItem("Live TV", Icons.Default.LiveTv),
    TabItem("Recherche", Icons.Default.Search),
    TabItem("Réglages", Icons.Default.Settings),
)

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onMovieClick: (streamId: Int, extension: String) -> Unit,
    onSeriesClick: (seriesId: Int) -> Unit,
    onChannelClick: (streamId: Int) -> Unit,
    onPlayLive: (url: String, title: String) -> Unit,
    onLogout: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (selectedTab) {
            0 -> MoviesTab(viewModel = viewModel, onMovieClick = onMovieClick, modifier = modifier)
            1 -> SeriesTab(viewModel = viewModel, onSeriesClick = onSeriesClick, modifier = modifier)
            2 -> ChannelsTab(viewModel = viewModel, onChannelClick = onChannelClick, onPlayLive = onPlayLive, modifier = modifier)
            3 -> SearchTab(
                viewModel = viewModel,
                onMovieClick = onMovieClick,
                onSeriesClick = onSeriesClick,
                onPlayLive = onPlayLive,
                modifier = modifier,
            )
            4 -> SettingsTab(viewModel = viewModel, onLogout = onLogout, modifier = modifier)
        }
    }
}
