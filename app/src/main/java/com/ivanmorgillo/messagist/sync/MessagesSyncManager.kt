package com.ivanmorgillo.messagist.sync

import android.content.Context
import arrow.core.Try
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

interface MessagesSyncManager {
    suspend fun sync(): Try<SyncResponseDTO>
}

class MessagesSyncManagerImpl(
    private val context: Context,
    private val gson: Gson
) : MessagesSyncManager {

    override suspend fun sync(): Try<SyncResponseDTO> = withContext(Dispatchers.IO) {
        loadJson()
            .flatMap { parseJson(it) }
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
}
