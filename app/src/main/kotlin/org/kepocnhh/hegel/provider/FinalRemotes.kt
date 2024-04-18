package org.kepocnhh.hegel.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Meta
import java.util.concurrent.TimeUnit

internal class FinalRemotes(
    private val serializer: Serializer,
) : Remotes {
    private val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private val URL = "http://192.168.88.225:40631" // todo

    override fun itemsSync(meta: Meta): ItemsSyncResponse {
        val request = Request.Builder()
            .url("$URL/v1/items/sync") // todo
            .header("Content-Type", "application/json")
            .post(serializer.meta.encode(meta).toRequestBody())
            .build()
        return client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    val bytes = response.body?.bytes() ?: TODO()
                    serializer.needUpdate.decode(bytes)
                }
                304 -> ItemsSyncResponse.NotModified
                else -> TODO("FinalRemotes:itemsSync:Unknown code ${response.code}!")
            }
        }
    }

    override fun itemsSyncMerge(request: ItemsSyncMergeRequest) {
        val request = Request.Builder()
            .url("$URL/v1/items/sync/merge") // todo
            .header("Content-Type", "application/json")
            .post(serializer.syncMerge.encode(request).toRequestBody())
            .build()
        return client.newCall(request).execute().use { response ->
            when (response.code) {
                else -> TODO("FinalRemotes:itemsSyncMerge:Unknown code ${response.code}!")
            }
        }
    }
}
