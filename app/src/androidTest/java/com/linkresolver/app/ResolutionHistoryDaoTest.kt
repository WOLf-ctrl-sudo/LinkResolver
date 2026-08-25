package com.linkresolver.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.linkresolver.app.data.local.AppDatabase
import com.linkresolver.app.data.local.ResolutionHistoryDao
import com.linkresolver.app.data.local.ResolutionHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResolutionHistoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ResolutionHistoryDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.resolutionHistoryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReadBack() = runTest {
        dao.insert(
            ResolutionHistoryEntity(
                originalUrl = "https://short.example/abc",
                finalUrl = "https://destination.example/page",
                provider = "Generic",
                status = "SUCCESS",
                redirectCount = 2,
                timestamp = 1000L
            )
        )

        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("https://destination.example/page", all.first().finalUrl)
    }

    @Test
    fun deleteById() = runTest {
        val id = dao.insert(
            ResolutionHistoryEntity(
                originalUrl = "https://short.example/abc",
                finalUrl = null,
                provider = "Generic",
                status = "TIMEOUT",
                redirectCount = 0,
                timestamp = 1000L
            )
        )
        dao.deleteById(id)
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun clearAllRemovesEverything() = runTest {
        repeat(3) { i ->
            dao.insert(
                ResolutionHistoryEntity(
                    originalUrl = "https://short.example/$i",
                    finalUrl = "https://destination.example/$i",
                    provider = "Generic",
                    status = "SUCCESS",
                    redirectCount = 1,
                    timestamp = i.toLong()
                )
            )
        }
        assertEquals(3, dao.observeAll().first().size)
        dao.clearAll()
        assertTrue(dao.observeAll().first().isEmpty())
    }
}
