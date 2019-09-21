package com.ivanmorgillo.messagist

import com.ivanmorgillo.messagist.MessageListEvent.OnResume
import com.ivanmorgillo.messagist.helpers.ScopedViewModel
import com.ivanmorgillo.messagist.helpers.exhaustive
import com.ivanmorgillo.messagist.sync.MessagesSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class MessageListEvent {
    object OnResume : MessageListEvent()
}

sealed class MessageListState

class MessageListViewModel(
    private val syncManager: MessagesSyncManager,
    private val database: Database
) : ScopedViewModel<MessageListState, MessageListEvent>() {

    override fun send(event: MessageListEvent) {
        when (event) {
            OnResume -> onResume()
        }.exhaustive
    }

    private fun onResume() {
        viewModelScope.launch(Dispatchers.IO) {
            syncManager.sync()

            val messages = database.messageQueries.messages().executeAsList()
            val messageList: List<ChatMessage> = messages.map {
                when {
                    it.id == 1L -> it.toMyChatMessage()
                    else -> it.toChatMessage()
                }
            }

            messageList.forEach { Timber.d(it.toString()) }
        }
    }
}
