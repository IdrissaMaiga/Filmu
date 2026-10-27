package com.iditechs.filmu.android.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iditechs.filmu.android.ui.player.PlayerScreen
import com.iditechs.filmu.android.ui.screens.ChannelDetailScreen
import com.iditechs.filmu.android.ui.screens.HomeScreen
import com.iditechs.filmu.android.ui.screens.LoginScreen
import com.iditechs.filmu.android.ui.screens.MovieDetailScreen
import com.iditechs.filmu.android.ui.screens.SeriesDetailScreen

@Composable
fun FilmuApp() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val startDest = if (isLoggedIn) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onMovieClick = { streamId, ext ->
                    navController.navigate("movie/$streamId/$ext")
                },
                onSeriesClick = { seriesId ->
                    navController.navigate("series/$seriesId")
                },
                onChannelClick = { streamId ->
                    navController.navigate("channel/$streamId")
                },
                onPlayLive = { url, title ->
                    navController.navigate(
                        "player?url=${java.net.URLEncoder.encode(url, "UTF-8")}&title=${java.net.URLEncoder.encode(title, "UTF-8")}"
                    )
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
            )
        }

        composable(
            "movie/{streamId}/{extension}",
            arguments = listOf(
                navArgument("streamId") { type = NavType.IntType },
                navArgument("extension") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getInt("streamId") ?: return@composable
            val extension = backStackEntry.arguments?.getString("extension") ?: "mp4"
            MovieDetailScreen(
                streamId = streamId,
                extension = extension,
                viewModel = viewModel,
                onPlay = { url, title ->
                    navController.navigate(
                        "player?url=${java.net.URLEncoder.encode(url, "UTF-8")}&title=${java.net.URLEncoder.encode(title, "UTF-8")}"
                    )
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            "series/{seriesId}",
            arguments = listOf(navArgument("seriesId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getInt("seriesId") ?: return@composable
            SeriesDetailScreen(
                seriesId = seriesId,
                viewModel = viewModel,
                onPlay = { url, title ->
                    navController.navigate(
                        "player?url=${java.net.URLEncoder.encode(url, "UTF-8")}&title=${java.net.URLEncoder.encode(title, "UTF-8")}"
                    )
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            "channel/{streamId}",
            arguments = listOf(navArgument("streamId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getInt("streamId") ?: return@composable
            ChannelDetailScreen(
                streamId = streamId,
                viewModel = viewModel,
                onPlay = { url, title ->
                    navController.navigate(
                        "player?url=${java.net.URLEncoder.encode(url, "UTF-8")}&title=${java.net.URLEncoder.encode(title, "UTF-8")}"
                    )
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            "player?url={url}&title={title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { backStackEntry ->
            val url = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("url") ?: "", "UTF-8"
            )
            val title = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "", "UTF-8"
            )
            PlayerScreen(
                streamUrl = url,
                title = title,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
