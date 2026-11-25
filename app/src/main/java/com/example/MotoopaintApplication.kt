package com.example

import android.app.Application
import com.example.data.local.MotoopaintDatabase
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.repository.BackupManager
import com.example.data.repository.LocalMediaStorage
import com.example.data.repository.MotoopaintRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(application: Application) {
    private val database: MotoopaintDatabase = MotoopaintDatabase.getInstance(application)
    val preferences: UserPreferencesRepository = UserPreferencesRepository(application)
    val mediaStorage: LocalMediaStorage = LocalMediaStorage(application)
    private val backupManager: BackupManager = BackupManager(application, database.dao())
    val repository: MotoopaintRepository = MotoopaintRepository(
        dao = database.dao(),
        mediaStorage = mediaStorage,
        backupManager = backupManager
    )
}

class MotoopaintApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.repository.ensureSeeded()
        }
    }
}
