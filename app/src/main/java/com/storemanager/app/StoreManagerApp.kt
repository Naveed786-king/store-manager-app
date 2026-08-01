package com.storemanager.app

import android.app.Application
import com.storemanager.app.data.AppDatabase
import com.storemanager.app.data.repository.StoreRepository

class StoreManagerApp : Application() {
    lateinit var repository: StoreRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = StoreRepository(db)
    }
}
