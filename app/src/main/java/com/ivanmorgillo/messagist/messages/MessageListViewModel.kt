package com.ivanmorgillo.messagist.messages

import arrow.syntax.function.pipe
import com.ivanmorgillo.messagist.helpers.ScopedViewModel
import com.ivanmorgillo.messagist.helpers.exhaustive
import com.ivanmorgillo.messagist.messages.ChatMessage.FriendMessage
import com.ivanmorgillo.messagist.messages.ChatMessage.MyChatMessage
import com.ivanmorgillo.messagist.messages.MessageListEvent.*
import com.ivanmorgillo.messagist.messages.MessageListState.ShowMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class MessageListEvent {
    data class OnElementAtPositionSwiped(val position: Int) : MessageListEvent()
    data class OnAttachmentClicked(val chatMessage: ChatMessage) : MessageListEvent()

    object OnResume : MessageListEvent()
    object OnListBottomReached : MessageListEvent()
}

sealed class MessageListState {
    data class ShowMessages(val messages: List<ChatMessage>) : MessageListState()
}

class MessageListViewModel(
    private val messageRepository: MessageRepository
) : ScopedViewModel<MessageListState, MessageListEvent>() {

    private val messages: MutableList<ChatMessage> = mutableListOf()

    override fun send(event: MessageListEvent) {
        when (event) {
            OnResume -> onResume()
            OnListBottomReached -> onListBottomReached()
            is OnElementAtPositionSwiped -> onElementSwiped(messages[event.position])
            is OnAttachmentClicked -> onAttachmentClicked(event.chatMessage)
        }.exhaustive
    }

    private fun onAttachmentClicked(chatMessage: ChatMessage) {
        Timber.d("onAttachmentClicked: $chatMessage")

        val messages = messages
            .map {
                when {
                    it.id == chatMessage.id -> removeAttachment(it)
                    else -> it
                }
            } pipe { addHeaders(it) }

        this.messages.clear()
        this.messages.addAll(messages)
        
        states.offer(ShowMessages(messages))
    }

    private fun removeAttachment(it: ChatMessage): ChatMessage {
        return when (it) {
            is FriendMessage.TextMessage,
            is MyChatMessage.TextMessage -> it
            is FriendMessage.Attachment -> {
                viewModelScope.launch {
                    messageRepository.deleteAttachment(it.attachmentId)
                }

                FriendMessage.TextMessage(
                    header = false,
                    content = it.content,
                    name = it.name,
                    avatarUrl = it.avatarUrl,
                    id = it.id
                )
            }
            is MyChatMessage.Attachment -> {
                viewModelScope.launch {
                    messageRepository.deleteAttachment(it.attachmentId)
                }

                MyChatMessage.TextMessage(
                    header = false,
                    content = it.content,
                    id = it.id,
                    name = it.name
                )
            }
        }.exhaustive
    }

    private fun onElementSwiped(chatMessage: ChatMessage) {
        Timber.d("onElementSwiped: $chatMessage")

        val messages = messages.filterNot { it.id == chatMessage.id } pipe { addHeaders(it) }
        this.messages.clear()
        this.messages.addAll(messages)

        viewModelScope.launch {
            messageRepository.deleteMessage(chatMessage)
        }

        states.offer(ShowMessages(messages))
    }

    private fun onResume() {
        viewModelScope.launch(Dispatchers.IO) {
            if (messages.isEmpty()) {
                val messageList: List<ChatMessage> = messageRepository.loadMessages(offset = 20) pipe { addHeaders(it) }
                messages.addAll(messageList)
            }

            states.offer(ShowMessages(messages))
        }
    }

    private fun onListBottomReached() {
        viewModelScope.launch(Dispatchers.IO) {
            val messageList: List<ChatMessage> = messageRepository.loadMessages(offset = (messages.size - 1).toLong())
            messages.addAll(messageList)
            states.offer(ShowMessages(addHeaders(messages)))
        }
    }
}

private fun addHeaders(messages: List<ChatMessage>): List<ChatMessage> {
    return messages
        .mapIndexed { index, item ->
            if (isFirstElement(index) || (currentNameIsPreviousName(item, messages, index))) enableHeader(item)
            else item
        }
}

private fun isFirstElement(index: Int) = index == 0

private fun currentNameIsPreviousName(
    item: ChatMessage,
    messages: List<ChatMessage>,
    index: Int
) = item.name != messages[index - 1].name

private fun enableHeader(chatMessage: ChatMessage): ChatMessage {
    return when (chatMessage) {
        is FriendMessage.TextMessage -> chatMessage.copy(header = true)
        is FriendMessage.Attachment -> chatMessage.copy(header = true)
        is MyChatMessage.TextMessage -> chatMessage.copy(header = true)
        is MyChatMessage.Attachment -> chatMessage.copy(header = true)
    }
}
