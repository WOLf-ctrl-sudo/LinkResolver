package com.linkresolver.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.linkresolver.app.ui.nav.LinkResolverNavHost
import com.linkresolver.app.ui.theme.LinkResolverTheme

class MainActivity : ComponentActivity() {

    private var sharedText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedText = extractSharedText(intent)

        val app = application as LinkResolverApplication

        setContent {
            LinkResolverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LinkResolverNavHost(
                        repository = app.repository,
                        sharedTextToResolve = sharedText
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedText = extractSharedText(intent)
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }
}
