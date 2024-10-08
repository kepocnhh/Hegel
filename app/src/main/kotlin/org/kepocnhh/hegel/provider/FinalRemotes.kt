package org.kepocnhh.hegel.provider

import okhttp3.OkHttpClient
import org.kepocnhh.hegel.provider.okhttp.OkHttpFilesRemotes
import org.kepocnhh.hegel.provider.okhttp.OkHttpItemsRemotes
import org.kepocnhh.hegel.provider.okhttp.OkHttpTLSTransmitter
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
            tlsTransmitter = OkHttpTLSTransmitter(
                client = client,
                address = address,
                tls = tls,
            ),
            loggers = loggers,
            serializer = serializer,
        )
    }

    override fun files(address: URL): FilesRemotes {
        return OkHttpFilesRemotes(
            tlsTransmitter = OkHttpTLSTransmitter(
                client = client,
                address = address,
                tls = tls,
            ),
            loggers = loggers,
        )
    }
}
