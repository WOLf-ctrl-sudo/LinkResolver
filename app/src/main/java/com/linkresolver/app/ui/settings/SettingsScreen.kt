package com.linkresolver.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkresolver.app.BuildConfig
import com.linkresolver.app.domain.resolver.ProviderRegistry
import com.linkresolver.app.ui.home.ResolverViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ResolverViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle(
        initialValue = com.linkresolver.app.data.local.AppSettings()
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            item { SectionHeader("General") }
            item {
                SwitchRow(
                    label = "Automatically copy final URL",
                    checked = settings.autoCopyFinalUrl
                ) { checked -> viewModel.updateSettings { it.copy(autoCopyFinalUrl = checked) } }
            }
            item {
                SwitchRow(
                    label = "Open final URL automatically",
                    checked = settings.autoOpenFinalUrl
                ) { checked -> viewModel.updateSettings { it.copy(autoOpenFinalUrl = checked) } }
            }
            item {
                SwitchRow(
                    label = "Save resolution history",
                    checked = settings.saveHistory
                ) { checked -> viewModel.updateSettings { it.copy(saveHistory = checked) } }
            }

            item { Divider(modifier = Modifier.padding(vertical = 12.dp)) }
            item { SectionHeader("Resolver") }
            item { InfoRow(label = "Maximum redirects", value = settings.maxRedirects.toString()) }
            item { InfoRow(label = "Request timeout", value = "${settings.timeoutSeconds} sec") }
            item {
                SwitchRow(
                    label = "Browser fallback",
                    checked = settings.browserFallbackEnabled
                ) { checked -> viewModel.updateSettings { it.copy(browserFallbackEnabled = checked) } }
            }

            item { Divider(modifier = Modifier.padding(vertical = 12.dp)) }
            item { SectionHeader("Privacy") }
            item {
                SwitchRow(
                    label = "Clear sensitive query parameters from history",
                    checked = settings.stripSensitiveParams
                ) { checked -> viewModel.updateSettings { it.copy(stripSensitiveParams = checked) } }
            }
            item {
                TextButton(onClick = { showClearDialog = true }) {
                    Text("Clear history")
                }
            }

            item { Divider(modifier = Modifier.padding(vertical = 12.dp)) }
            item { SectionHeader("About") }
            item { InfoRow(label = "Version", value = BuildConfig.VERSION_NAME) }
            item {
                Text(
                    "Resolves shortened URLs using publicly exposed HTTP redirects only. " +
                        "It never solves CAPTCHAs or bypasses browser-verification or access controls.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item { SectionHeader("Resolver Providers") }
            items(ProviderRegistry.supportedProviderNames()) { name ->
                Text("✓ $name", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This will permanently delete every saved resolution.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.clearHistory() }
                    showClearDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
