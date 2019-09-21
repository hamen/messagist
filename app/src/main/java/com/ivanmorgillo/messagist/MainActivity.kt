package com.ivanmorgillo.messagist

import android.os.Bundle
import com.ivanmorgillo.messagist.helpers.ScopedActivity
import com.ivanmorgillo.messagist.sync.MessagesSyncManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : ScopedActivity() {
    private val syncManager: MessagesSyncManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityScope.launch {
            val response = syncManager.sync()

            response.fold(
                { Timber.e(it) },
                { Timber.d(it.toString()) }
            )
        }
    }
}
