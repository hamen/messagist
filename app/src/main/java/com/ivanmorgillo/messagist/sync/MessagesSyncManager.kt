package com.ivanmorgillo.messagist.sync

import android.content.Context
import android.content.SharedPreferences
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
    private val NEEDS_TO_SYNC_KEY = "NEEDS_TO_SYNC"

    private val database: Database by lazy { Database(sqlDriver) }
    private val sharedPreferences: SharedPreferences by lazy { context.getSharedPreferences("sync", Context.MODE_PRIVATE) }

    override suspend fun sync(): Try<Boolean> = withContext(Dispatchers.IO) {
        if (needsToSync()) {
            loadJson()
                .flatMap { parseJson(it) }
                .flatMap {
                    binding {
                        storeUsers(it.users)
                        storeMessages(it.messages)
                        it.messages.forEach {
                            storeAttachments(it.id, it.attachments)
                        }
                        syncHappened()
                    }.flatMap { true.success() }
                }
        } else {
            true.success()
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

    suspend fun needsToSync(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(NEEDS_TO_SYNC_KEY, true)
    }

    private fun syncHappened(): Try<Boolean> = Try {
        sharedPreferences.edit().putBoolean(NEEDS_TO_SYNC_KEY, false).commit()
    }
}
