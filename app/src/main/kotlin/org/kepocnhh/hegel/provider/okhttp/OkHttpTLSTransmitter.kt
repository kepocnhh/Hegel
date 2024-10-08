package org.kepocnhh.hegel.provider.okhttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kepocnhh.hegel.entity.NotModifiedException
import sp.kx.http.TLSEnvironment
import sp.kx.http.TLSTransmitter
import java.net.URL

internal class OkHttpTLSTransmitter(
    private val client: OkHttpClient,
    private val address: URL,
    private val tls: TLSEnvironment,
) {
    fun <T : Any> execute(
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
}
