package com.ivanmorgillo.messagist

import android.os.Bundle
import com.google.gson.Gson
import com.ivanmorgillo.messagist.helpers.ScopedActivity
import com.ivanmorgillo.messagist.sync.MessagesSyncManagerImpl
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ScopedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityScope.launch {
            val response = MessagesSyncManagerImpl(this@MainActivity, Gson()).sync()

            response.fold(
                { Timber.e(it) },
                { Timber.d(it.toString()) }
            )
        }
    }
}
