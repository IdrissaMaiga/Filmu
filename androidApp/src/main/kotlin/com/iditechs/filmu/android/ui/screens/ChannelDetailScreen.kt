package com.iditechs.filmu.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.iditechs.filmu.EpgEntry
import com.iditechs.filmu.android.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelDetailScreen(
    streamId: Int,
    viewModel: AppViewModel,
    onPlay: (url: String, title: String) -> Unit,
    onBack: () -> Unit,
) {
    val streams by viewModel.liveStreams.collectAsState()
    val stream = remember(streams, streamId) { streams.find { it.stream_id == streamId } }

    var epgEntries by remember { mutableStateOf<List<EpgEntry>>(emptyList()) }
    var epgLoading by remember { mutableStateOf(false) }

    LaunchedEffect(streamId) {
        val epgId = stream?.epg_channel_id
        if (!epgId.isNullOrBlank()) {
            epgLoading = true
            epgEntries = viewModel.getEpg(streamId)
            epgLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stream?.name ?: "") },
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
        if (stream == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val ch = stream

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val icon = ch.stream_icon
                    if (!icon.isNullOrBlank()) {
                        AsyncImage(
                            model = icon,
                            contentDescription = ch.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ch.name ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        if (ch.tv_archive == 1) {
                            Text(
                                text = "Replay disponible (${ch.tv_archive_duration ?: 0}j)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val url = viewModel.getLiveStreamUrl(ch.stream_id)
                        if (url != null) onPlay(url, ch.name ?: "Live")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("  Regarder en direct")
                }
            }

            if (epgLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (epgEntries.isNotEmpty()) {
                item {
                    Text(
                        text = "Programme",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                items(epgEntries) { entry ->
                    EpgRow(entry)
                }
            }
        }
    }
}

@Composable
private fun EpgRow(entry: EpgEntry) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val startTs = entry.start_timestamp?.toLongOrNull()
                val endTs = entry.stop_timestamp?.toLongOrNull()
                if (startTs != null) {
                    val timeText = buildString {
                        append(dateFormat.format(Date(startTs * 1000)))
                        if (endTs != null) {
                            append(" - ")
                            append(dateFormat.format(Date(endTs * 1000)))
                        }
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = entry.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
            val desc = entry.description
            if (!desc.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}
