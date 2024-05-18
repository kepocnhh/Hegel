package org.kepocnhh.hegel.provider.okhttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.provider.ItemsRemotes
import org.kepocnhh.hegel.provider.Serializer
import java.net.URL

internal class OkHttpItemsRemotes(
    private val client: OkHttpClient,
    private val serializer: Serializer,
    private val url: URL,
) : ItemsRemotes {
    override fun sync(request: ItemsSyncRequest): ItemsSyncResponse {
        return client.newCall(
            Request.Builder()
                .url(URL(url, "v1/items/sync"))
                .header("Content-Type", "application/json")
                .post(serializer.remote.syncRequest.encode(request).toRequestBody())
                .build()
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val bytes = response.body?.bytes() ?: TODO()
                    serializer.remote.needUpdate.decode(bytes)
                }
                304 -> ItemsSyncResponse.NotModified
                else -> TODO("FinalRemotes:itemsSync:Unknown code ${response.code}!")
            }
        }
    }

    override fun merge(request: ItemsSyncMergeRequest): ItemsSyncMergeResponse {
        return client.newCall(
            Request.Builder()
                .url(URL(url, "v1/items/merge"))
                .header("Content-Type", "application/json")
                .post(serializer.remote.syncMerge.encode(request).toRequestBody())
                .build()
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val bytes = response.body?.bytes() ?: TODO()
                    serializer.remote.mergeResponse.decode(bytes)
                }
                else -> TODO("FinalRemotes:itemsSyncMerge:Unknown code ${response.code}!")
            }
        }
    }
}
