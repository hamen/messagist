package com.ivanmorgillo.messagist.messages

import com.ivanmorgillo.messagist.Database
import com.ivanmorgillo.messagist.Message
import com.ivanmorgillo.messagist.sync.MessagesSyncManager

interface MessageRepository {
    suspend fun loadMessages(limit: Long = 20, offset: Long): List<ChatMessage>
    suspend fun deleteMessage(chatMessage: ChatMessage)
    suspend fun deleteAttachment(attachmentId: String)
}

class MessageRepositoryImpl(
    private val syncManager: MessagesSyncManager,
    private val database: Database
) : MessageRepository {
    override suspend fun loadMessages(limit: Long, offset: Long): List<ChatMessage> {
        syncManager.sync()

        val messages: List<Message> = database.messageQueries.message(limit, offset).executeAsList()
        return messages
            .map {
                when {
                    it.userId == 1L -> it.toMyChatMessage()
                    else -> it.toChatMessage()
                }
            }
    }

    override suspend fun deleteMessage(chatMessage: ChatMessage) = database.messageQueries.delete(chatMessage.id)

    override suspend fun deleteAttachment(attachmentId: String) = database.attachmentQueries.delete(attachmentId)
}
