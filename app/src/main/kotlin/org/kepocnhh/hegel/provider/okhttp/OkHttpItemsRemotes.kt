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
import sp.kx.http.TLSEnvironment
import sp.kx.http.TLSTransmitter
import java.net.URL

internal class OkHttpItemsRemotes(
    private val client: OkHttpClient,
    private val serializer: Serializer,
    private val address: URL,
    private val tls: TLSEnvironment,
) : ItemsRemotes {
    private fun <T : Any> execute(
        method: String,
        query: String,
        encoded: ByteArray,
        decode: (ByteArray) -> T,
    ): T {
        // todo util
        val methodCode: Byte = TLSEnvironment.getMethodCode(method = method)
        val encodedQuery = query.toByteArray()
        val tlsTransmitter = TLSTransmitter.build(
            env = tls,
            methodCode = methodCode,
            encodedQuery = encodedQuery,
            encoded = encoded,
        )
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, query))
                .method(method, tlsTransmitter.body.toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body?.bytes() ?: error("No body!")
                    val responseEncoded = TLSTransmitter.fromResponse(
                        env = tls,
                        methodCode = methodCode,
                        encodedQuery = encodedQuery,
                        secretKey = tlsTransmitter.secretKey,
                        requestID = tlsTransmitter.id,
                        responseCode = response.code,
                        message = response.message,
                        body = body,
                    )
                    decode(responseEncoded)
                }
                304 -> throw NotModifiedException()
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun sync(request: ItemsSyncRequest): ItemsSyncResponse {
        return execute(
            method = "POST",
            query = "/v1/items/sync",
            encoded = serializer.remote.syncRequest.encode(request),
            decode = serializer.remote.syncResponse::decode,
        )
    }

    override fun merge(request: ItemsMergeRequest): ItemsMergeResponse {
        return execute(
            method = "POST",
            query = "/v1/items/merge",
            encoded = serializer.remote.mergeRequest.encode(request),
            decode = serializer.remote.mergeResponse::decode,
        )
    }
}
