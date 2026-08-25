package com.linkresolver.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkresolver.app.R
import com.linkresolver.app.data.local.ResolutionHistoryEntity
import com.linkresolver.app.domain.resolver.ProviderRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ResolverViewModel,
    onOpenResult: () -> Unit,
    onNavigateToBatch: () -> Unit
) {
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle(initialValue = emptyList<ResolutionHistoryEntity>())
    val clipboardManager = LocalClipboardManager.current

    // Navigate to the result screen once a resolve finishes.
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is ResolverUiState.Success || uiState is ResolverUiState.Error) {
            onOpenResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Expand shortened URLs safely",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = viewModel::onUrlChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Shortened URL input" },
                placeholder = { Text("Paste shortened URL...") },
                leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null) },
                trailingIcon = {
                    Row {
                        if (urlInput.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearInput() }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { viewModel.onUrlChanged(it) }
                        }) {
                            Icon(Icons.Filled.ContentPaste, contentDescription = "Paste")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.resolve() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = urlInput.isNotBlank() && uiState !is ResolverUiState.Loading
            ) {
                Text("Resolve URL")
            }

            if (uiState is ResolverUiState.Loading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text(
                    (uiState as ResolverUiState.Loading).message,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Supported providers: ${ProviderRegistry.supportedProviderNames().size - 1}+",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent resolutions", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onNavigateToBatch) {
                    Text("Batch Resolve")
                }
            }
            Spacer(Modifier.height(8.dp))

            if (history.isEmpty()) {
                Text(
                    "No history yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    val recent = history.take(5)
                    items(items = recent) { entry ->
                        HistoryPreviewCard(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryPreviewCard(entry: ResolutionHistoryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(entry.provider, style = MaterialTheme.typography.titleSmall)
            Text(
                entry.finalUrl ?: entry.originalUrl,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}
