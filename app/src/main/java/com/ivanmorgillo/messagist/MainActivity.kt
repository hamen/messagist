package com.ivanmorgillo.messagist

import android.os.Bundle
import com.ivanmorgillo.messagist.helpers.ScopedActivity
import com.ivanmorgillo.messagist.helpers.exhaustive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : ScopedActivity() {
    private val viewmodel: MessageListViewModel by viewModel()

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityScope.launch {
            viewmodel.states.consumeEach { state ->
                when (state) {
                    else -> TODO()
                }.exhaustive
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewmodel.send(MessageListEvent.OnResume)
    }
}
