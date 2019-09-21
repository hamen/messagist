package com.ivanmorgillo.messagist.sync

import android.content.Context
import arrow.core.Try
import arrow.core.extensions.`try`.monad.binding
import arrow.core.success
import com.google.gson.Gson
import com.ivanmorgillo.messagist.Database
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.charset.Charset

interface MessagesSyncManager {
    suspend fun sync(): Try<Boolean>
}

class MessagesSyncManagerImpl(
    private val context: Context,
    private val gson: Gson,
    private val sqlDriver: SqlDriver
) : MessagesSyncManager {

    private val database: Database by lazy { Database(sqlDriver) }

    override suspend fun sync(): Try<Boolean> = withContext(Dispatchers.IO) {
        loadJson()
            .flatMap { parseJson(it) }
            .flatMap {
                binding {
                    storeUsers(it.users)
                    storeMessages(it.messages)
                    it.messages.forEach {
                        storeAttachments(it.id, it.attachments)
                    }
                }.flatMap { true.success() }
            }
    }

    private fun loadJson(): Try<String> {
        return Try {
            val inputStream = context.assets.open("data/data.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        }
    }

    private fun parseJson(json: String): Try<SyncResponseDTO> = Try { gson.fromJson(json, SyncResponseDTO::class.java) }

    private fun storeUsers(users: List<UserDTO>) = Try {
        users.forEach {
            Timber.d("Storing user: $it")
            database.userQueries.insert(it.id, it.name, it.avatarUrl)
        }
        users
    }

    private fun storeMessages(messages: List<MessageDTO>) = Try {
        messages.forEach {
            Timber.d("Storing message: $it")
            database.messageQueries.insert(it.id, it.userId, it.content)
        }
        messages
    }

    private fun storeAttachments(messageId: Long, attachments: List<MessageDTO.AttachmentDTO>) = Try {
        attachments.forEach {
            Timber.d("Storing attachment: $it")
            database.attachmentQueries.insert(
                id = it.id,
                messageid = messageId,
                title = it.title,
                url = it.title,
                thumbnail = it.thumbnailUrl
            )
        }
    }
}
