package org.kepocnhh.hegel.provider

import okhttp3.OkHttpClient
import org.kepocnhh.hegel.provider.okhttp.OkHttpItemsRemotes
import sp.kx.http.TLSEnvironment
import java.net.URL
import java.util.concurrent.TimeUnit

internal class FinalRemotes(
    private val serializer: Serializer,
    private val tls: TLSEnvironment,
    private val loggers: Loggers,
) : Remotes {
    private val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    override fun items(address: URL): ItemsRemotes {
        return OkHttpItemsRemotes(
            client = client,
            serializer = serializer,
            address = address,
            tls = tls,
            logger = loggers.create("[Items]"),
        )
    }
}
