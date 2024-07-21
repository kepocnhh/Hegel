package org.kepocnhh.hegel.provider.okhttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.NotModifiedException
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
//                    println("on response sync: (${bytes.size})${bytes.toHEX()}") // todo
                    serializer.remote.syncResponse.decode(bytes)
                }
                304 -> throw NotModifiedException()
                else -> TODO("FinalRemotes:itemsSync:Unknown code ${response.code}!")
            }
        }
    }

    override fun merge(request: ItemsMergeRequest): ItemsMergeResponse {
        return client.newCall(
            Request.Builder()
                .url(URL(url, "v1/items/merge"))
                .header("Content-Type", "application/json")
                .post(serializer.remote.mergeRequest.encode(request).toRequestBody())
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
