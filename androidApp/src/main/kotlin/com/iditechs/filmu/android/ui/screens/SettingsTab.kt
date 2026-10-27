package com.iditechs.filmu.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iditechs.filmu.android.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsTab(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val serverInfo by viewModel.serverInfo.collectAsState()
    val userInfo = serverInfo?.user_info
    val serverData = serverInfo?.server_info

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Réglages",
            style = MaterialTheme.typography.headlineMedium,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Serveur",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("URL", serverData?.url ?: "-")
                InfoRow("Port", serverData?.port ?: "-")
                InfoRow("Fuseau", serverData?.timezone ?: "-")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Abonnement",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Utilisateur", userInfo?.username ?: "-")
                InfoRow("Statut", userInfo?.status ?: "-")
                InfoRow("Connexions max", userInfo?.max_connections ?: "-")
                InfoRow("Connexions actives", userInfo?.active_cons ?: "-")

                val expiry = userInfo?.exp_date
                if (expiry != null) {
                    val expiryFormatted = try {
                        val epoch = expiry.toLongOrNull()
                        if (epoch != null) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(Date(epoch * 1000))
                        } else expiry
                    } catch (_: Exception) { expiry }
                    InfoRow("Expiration", expiryFormatted)
                }

                val formats = userInfo?.allowed_output_formats
                if (!formats.isNullOrEmpty()) {
                    InfoRow("Formats", formats.joinToString(", "))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.logout(onLogout) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Déconnexion")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Filmu v1.0.0 — IDITECHS",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
