package org.kepocnhh.hegel.provider.okhttp

import org.kepocnhh.hegel.entity.FileRequest
import org.kepocnhh.hegel.provider.FilesRemotes
import org.kepocnhh.hegel.provider.Loggers
import org.kepocnhh.hegel.provider.Serializer

internal class OkHttpFilesRemotes(
    private val tlsTransmitter: OkHttpTLSTransmitter,
    loggers: Loggers,
    private val serializer: Serializer,
) : FilesRemotes {
    private val logger = loggers.create("[Files]")

    override fun getBytes(request: FileRequest): ByteArray {
        logger.debug("get bytes by: $request")
        return tlsTransmitter.execute(
            method = "POST",
            query = "/v1/bytes",
            encoded = serializer.remote.fileRequest.encode(request),
            decode = {it},
        )
    }
}
