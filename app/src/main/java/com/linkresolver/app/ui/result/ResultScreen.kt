package com.linkresolver.app.ui.result

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.linkresolver.app.domain.model.RedirectStep
import com.linkresolver.app.domain.model.ResolveResult
import com.linkresolver.app.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: ResolveResult,
    onBack: () -> Unit,
    onResolveAgain: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (result.isSuccess) "Resolution Complete" else "Unable to Resolve") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Provider", style = MaterialTheme.typography.labelMedium)
                    StatusBadge(status = result.status)
                }
                Text(result.provider, style = MaterialTheme.typography.titleMedium)
            }

            item {
                LabeledUrl(label = "Original URL", url = result.originalUrl)
            }

            if (result.isSuccess && result.finalUrl != null) {
                item {
                    LabeledUrl(label = "Final URL", url = result.finalUrl)
                }

                item {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        InfoStat(label = "Redirects", value = (result.redirects.size - 1).coerceAtLeast(0).toString())
                        InfoStat(label = "Time", value = "${"%.2f".format(result.elapsedMs / 1000.0)}s")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(result.finalUrl))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(Modifier.height(0.dp))
                            Text(" Copy")
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, result.finalUrl.toUri()))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                            Text(" Open")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, result.finalUrl)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share final URL"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Text(" Share")
                        }
                        OutlinedButton(onClick = onResolveAgain, modifier = Modifier.weight(1f)) {
                            Text("Resolve Again")
                        }
                    }
                }

                item {
                    Text("Redirect Chain", style = MaterialTheme.typography.titleMedium)
                }

                items(result.redirects) { step ->
                    RedirectStepRow(step) {
                        clipboard.setText(AnnotatedString(step.url))
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Unable to resolve this link", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text("Reason", style = MaterialTheme.typography.labelMedium)
                            Text(
                                result.errorMessage ?: friendlyMessageFor(result),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, result.originalUrl.toUri()))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.OpenInBrowser, contentDescription = null)
                            Text(" Open in Browser")
                        }
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(result.originalUrl))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy Original URL")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledUrl(label: String, url: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(url, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InfoStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RedirectStepRow(step: RedirectStep, onCopy: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(step.url, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            if (step.statusCode != null) {
                Text(
                    "${step.statusCode} ${step.statusText.orEmpty()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun friendlyMessageFor(result: ResolveResult): String = when (result.status) {
    com.linkresolver.app.domain.model.ResolverStatus.TIMEOUT ->
        "The website took too long to respond. Try again or open it in your browser."
    com.linkresolver.app.domain.model.ResolverStatus.NETWORK_ERROR ->
        "Check your internet connection and try again."
    com.linkresolver.app.domain.model.ResolverStatus.REDIRECT_LOOP ->
        "The website created a redirect loop."
    com.linkresolver.app.domain.model.ResolverStatus.PROTECTED_REDIRECT ->
        "This website requires browser verification or interaction. The app cannot resolve it automatically."
    else -> "The destination could not be determined."
}
