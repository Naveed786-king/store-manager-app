package com.storemanager.app.util

import android.content.Context
import androidx.core.content.FileProvider
import com.storemanager.app.data.AppDatabase
import java.io.File
import android.net.Uri

object BackupUtils {

    fun backup(context: Context): Uri {
        AppDatabase.closeAndClear()
        val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
        val dir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val backupFile = File(dir, "backup_${System.currentTimeMillis()}.db")
        dbFile.copyTo(backupFile, overwrite = true)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
    }

    fun restore(context: Context, uri: Uri): Boolean {
        return try {
            AppDatabase.closeAndClear()
            val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
            context.contentResolver.openInputStream(uri)?.use { input ->
                dbFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
