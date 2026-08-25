package com.linkresolver.app.ui.history

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkresolver.app.data.local.ResolutionHistoryEntity
import com.linkresolver.app.ui.home.ResolverViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: ResolverViewModel) {
    val history by viewModel.history.collectAsStateWithLifecycle(initialValue = emptyList<ResolutionHistoryEntity>())
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    val groupedHistory = remember(history) { groupHistoryByDate(history) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { showClearDialog = true }) {
                            Text("Clear all")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No history yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                groupedHistory.forEach { (date, entries) ->
                    stickyHeader {
                        DateHeader(date)
                    }
                    items(entries, key = { it.id }) { entry ->
                        HistoryRow(
                            entry = entry,
                            onOpen = {
                                val target = entry.finalUrl ?: entry.originalUrl
                                context.startActivity(Intent(Intent.ACTION_VIEW, target.toUri()))
                            },
                            onCopy = {
                                clipboard.setText(AnnotatedString(entry.finalUrl ?: entry.originalUrl))
                            },
                            onShare = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, entry.finalUrl ?: entry.originalUrl)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share URL"))
                            },
                            onDelete = { scope.launch { viewModel.deleteHistoryEntry(entry) } }
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all history?") },
            text = { Text("This will permanently delete every saved resolution. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.clearHistory() }
                    showClearDialog = false
                }) { Text("Clear all") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HistoryRow(
    entry: ResolutionHistoryEntity,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.provider, style = MaterialTheme.typography.titleSmall)
                Text(
                    formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                entry.finalUrl ?: "Not resolved (${entry.status})",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Filled.OpenInBrowser, contentDescription = "Open")
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(
                text = date,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

private fun groupHistoryByDate(history: List<ResolutionHistoryEntity>): Map<String, List<ResolutionHistoryEntity>> {
    val dayFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val today = dayFormatter.format(Date())
    val yesterday = dayFormatter.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

    return history.groupBy {
        val dateStr = dayFormatter.format(Date(it.timestamp))
        when (dateStr) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> dateStr
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}
