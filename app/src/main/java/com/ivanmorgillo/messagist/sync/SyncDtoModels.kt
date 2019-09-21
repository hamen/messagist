package com.ivanmorgillo.messagist.sync

import com.google.gson.annotations.SerializedName

data class MessageDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("content") val content: String,
    @SerializedName("attachments") val attachments: List<AttachmentDTO>
) {
    data class AttachmentDTO(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("url") val url: String,
        @SerializedName("thumbnailUrl") val thumbnailUrl: String
    )
}

data class UserDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("avatarId") val avatarUrl: String
)

data class SyncResponseDTO(
    @SerializedName("messages") val messages: List<MessageDTO>,
    @SerializedName("users") val users: List<UserDTO>
)
