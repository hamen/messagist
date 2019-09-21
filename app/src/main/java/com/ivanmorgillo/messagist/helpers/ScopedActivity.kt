package com.ivanmorgillo.messagist.helpers

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

abstract class ScopedActivity : AppCompatActivity() {
    private val job = Job()
    val activityScope: CoroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
