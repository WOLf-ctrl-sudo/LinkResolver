package com.linkresolver.app

import android.app.Application
import com.linkresolver.app.data.local.AppDatabase
import com.linkresolver.app.data.local.SettingsStore
import com.linkresolver.app.data.repository.ResolverRepository

class LinkResolverApplication : Application() {

    lateinit var repository: ResolverRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        val settingsStore = SettingsStore(this)
        repository = ResolverRepository(
            historyDao = database.resolutionHistoryDao(),
            settingsStore = settingsStore
        )
    }
}
