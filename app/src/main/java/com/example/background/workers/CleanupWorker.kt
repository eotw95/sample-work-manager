package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.OUTPUT_PATH
import java.io.File

class CleanupWorker(ctx: Context, param: WorkerParameters): Worker(ctx, param) {
    companion object {
        private const val TAG = "CleanupWorker"
    }
    override fun doWork(): Result {
        makeStatusNotification("Cleaning up old temporary files", applicationContext)

        return try {
            val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDir.exists()) {
                val entries = outputDir.listFiles()
                for (entry in entries) {
                    val fileName = entry.name
                    if (fileName.isNotEmpty() && fileName.endsWith(".png")) {
                        val deleted = entry.delete()
                        Log.d(TAG, "Delete $fileName is $deleted")
                    }
                }
            }
            Result.success()
        } catch(exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }

}