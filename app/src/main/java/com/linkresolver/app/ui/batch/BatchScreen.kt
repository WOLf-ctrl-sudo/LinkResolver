package com.linkresolver.app.ui.batch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linkresolver.app.domain.model.ResolverStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScreen(viewModel: BatchViewModel) {
    val input by viewModel.input.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Batch Resolve") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Paste one URL per line. Up to ${BatchViewModel.MAX_CONCURRENT} are resolved at a time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = input,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("https://short1.example/abc\nhttps://short2.example/xyz") },
                enabled = state !is BatchUiState.Running
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.resolveAll() },
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank() && state !is BatchUiState.Running
            ) {
                Text("Resolve All")
            }

            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))

            when (val current = state) {
                is BatchUiState.Idle -> Unit

                is BatchUiState.Running -> {
                    Text("${current.total} URLs")
                    LinearProgressIndicator(
                        progress = { if (current.total == 0) 0f else current.completed / current.total.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${current.completed} / ${current.total} completed")
                }

                is BatchUiState.Done -> {
                    val succeeded = current.results.count { it.isSuccess }
                    val failed = current.results.size - succeeded
                    Text("${current.results.size} URLs")
                    Text("✓ $succeeded Resolved")
                    if (failed > 0) Text("✕ $failed Failed")

                    androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.results) { result ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        if (result.status == ResolverStatus.SUCCESS) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                        contentDescription = if (result.status == ResolverStatus.SUCCESS) "Resolved" else "Failed"
                                    )
                                    Column {
                                        Text(result.originalUrl, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                        Text(
                                            result.finalUrl ?: (result.errorMessage ?: result.status.name),
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
