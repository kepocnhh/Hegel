package org.kepocnhh.hegel.provider

import okhttp3.OkHttpClient
import org.kepocnhh.hegel.provider.okhttp.OkHttpItemsRemotes
import java.net.URL
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

    override fun items(url: URL): ItemsRemotes {
        return OkHttpItemsRemotes(
            client = client,
            serializer = serializer,
            url = url,
        )
    }
}
