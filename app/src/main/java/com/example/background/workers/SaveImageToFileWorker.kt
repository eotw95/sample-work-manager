package com.example.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SaveImageToFileWorker(ctx: Context, param: WorkerParameters): Worker(ctx, param) {
    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

}