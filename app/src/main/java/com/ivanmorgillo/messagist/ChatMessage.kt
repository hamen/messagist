package com.ivanmorgillo.messagist

sealed class ChatMessage {
    abstract val id: Long

    sealed class FriendMessage : ChatMessage() {
        data class TextMessage(
            val content: String,
            val name: String,
            val avatarUrl: String,
            override val id: Long
        ) : FriendMessage()

        data class Attachment(
            val title: String,
            val url: String,
            val thumbnail: String,
            val name: String,
            val avatarUrl: String,
            override val id: Long
        ) : FriendMessage()
    }

    sealed class MyChatMessage : ChatMessage() {
        data class TextMessage(
            val content: String,
            override val id: Long
        ) : MyChatMessage()

        data class Attachment(
            val title: String,
            val url: String,
            val thumbnail: String,
            override val id: Long
        ) : MyChatMessage()
    }
}


fun Messages.toMyChatMessage(): ChatMessage.MyChatMessage {
    return when (thumbnail) {
        null -> toMyTextMessage()
        else -> toMyAttachmentMessage()
    }
}

fun Messages.toMyAttachmentMessage(): ChatMessage.MyChatMessage.Attachment {
    return ChatMessage.MyChatMessage.Attachment(
        title = title!!,
        url = url!!,
        thumbnail = thumbnail!!,
        id = id
    )
}

fun Messages.toMyTextMessage(): ChatMessage.MyChatMessage.TextMessage {
    return ChatMessage.MyChatMessage.TextMessage(
        content = content,
        id = id
    )
}

fun Messages.toChatMessage(): ChatMessage.FriendMessage {
    return when (thumbnail) {
        null -> toTextMessage()
        else -> toAttachmentMessage()
    }
}

fun Messages.toAttachmentMessage(): ChatMessage.FriendMessage.Attachment {
    return ChatMessage.FriendMessage.Attachment(
        title = title!!,
        url = url!!,
        thumbnail = thumbnail!!,
        name = name!!,
        avatarUrl = avatarUrl!!,
        id = id
    )
}

fun Messages.toTextMessage(): ChatMessage.FriendMessage.TextMessage {
    return ChatMessage.FriendMessage.TextMessage(
        content = content,
        name = name!!,
        avatarUrl = avatarUrl!!,
        id = id
    )
}

