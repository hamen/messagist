package com.ivanmorgillo.messagist.helpers

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel

abstract class ScopedViewModel<S, E> : ViewModel() {
    companion object {
        private const val STATES_CHANNEL_CAPACITY = 5
    }

    private val job = Job()
    protected val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)
    val states = Channel<S>(STATES_CHANNEL_CAPACITY)

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    abstract fun send(event: E)
}
