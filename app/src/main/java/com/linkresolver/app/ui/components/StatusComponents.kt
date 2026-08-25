package com.linkresolver.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.linkresolver.app.domain.model.ResolverStatus

/**
 * Small status pill. Uses both an icon shape AND a text label so
 * success/failure is never communicated by color alone.
 */
@Composable
fun StatusBadge(status: ResolverStatus, modifier: Modifier = Modifier) {
    val (icon, label, containerColor, contentColor) = when (status) {
        ResolverStatus.SUCCESS -> BadgeStyle(
            Icons.Filled.CheckCircle, "Resolved",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        ResolverStatus.PROTECTED_REDIRECT -> BadgeStyle(
            Icons.Filled.Warning, "Needs browser",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ResolverStatus.NO_REDIRECT_FOUND -> BadgeStyle(
            Icons.Filled.Warning, "No Redirect",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        else -> BadgeStyle(
            Icons.Filled.Error, "Failed",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
    }

    Surface(
        modifier = modifier.semantics { contentDescription = label },
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

private data class BadgeStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
)

private operator fun BadgeStyle.component1() = icon
private operator fun BadgeStyle.component2() = label
private operator fun BadgeStyle.component3() = containerColor
private operator fun BadgeStyle.component4() = contentColor
