package com.ivanmorgillo.messagist.messages

import com.ivanmorgillo.messagist.Message
import com.ivanmorgillo.messagist.messages.ChatMessage.FriendMessage
import com.ivanmorgillo.messagist.messages.ChatMessage.MyChatMessage

sealed class ChatMessage {
    abstract val id: Long
    abstract val header: Boolean
    abstract val name: String

    sealed class FriendMessage : ChatMessage() {
        data class TextMessage(
            override val header: Boolean = false,
            val content: String,
            override val name: String,
            val avatarUrl: String,
            override val id: Long
        ) : FriendMessage()

        data class Attachment(
            override val header: Boolean = false,
            override val name: String,
            val content: String,
            val title: String,
            val url: String,
            val thumbnail: String,
            val avatarUrl: String,
            override val id: Long,
            val attachmentId: String
        ) : FriendMessage()

    }

    sealed class MyChatMessage : ChatMessage() {
        data class TextMessage(
            override val header: Boolean = false,
            val content: String,
            override val id: Long,
            override val name: String
        ) : MyChatMessage()

        data class Attachment(
            override val header: Boolean = false,
            val title: String,
            val url: String,
            val thumbnail: String,
            val content: String,
            override val id: Long,
            override val name: String,
            val attachmentId: String
        ) : MyChatMessage()
    }
}


fun Message.toMyChatMessage(): MyChatMessage {
    return when (thumbnail) {
        null -> toMyTextMessage()
        else -> toMyAttachmentMessage()
    }
}

fun Message.toMyAttachmentMessage(): MyChatMessage.Attachment {
    return MyChatMessage.Attachment(
        title = title!!,
        url = url!!,
        thumbnail = thumbnail!!,
        id = id,
        name = name!!,
        content = content,
        attachmentId = attachmentId!!
    )
}

fun Message.toMyTextMessage(): MyChatMessage.TextMessage {
    return MyChatMessage.TextMessage(
        content = content,
        id = id,
        name = name!!
    )
}

fun Message.toChatMessage(): FriendMessage {
    return when (thumbnail) {
        null -> toTextMessage()
        else -> toAttachmentMessage()
    }
}

fun Message.toAttachmentMessage(): FriendMessage.Attachment {
    return FriendMessage.Attachment(
        title = title!!,
        url = url!!,
        thumbnail = thumbnail!!,
        avatarUrl = avatarUrl!!,
        id = id,
        name = name!!,
        content = content,
        attachmentId = attachmentId!!
    )
}

fun Message.toTextMessage(): FriendMessage.TextMessage {
    return FriendMessage.TextMessage(
        content = content,
        name = name!!,
        avatarUrl = avatarUrl!!,
        id = id
    )
}

