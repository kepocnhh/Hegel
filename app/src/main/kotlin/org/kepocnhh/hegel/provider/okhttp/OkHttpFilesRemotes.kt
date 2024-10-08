package org.kepocnhh.hegel.provider.okhttp

import org.kepocnhh.hegel.provider.FilesRemotes
import org.kepocnhh.hegel.provider.Loggers
import sp.kx.bytes.toByteArray
import java.util.UUID

internal class OkHttpFilesRemotes(
    private val tlsTransmitter: OkHttpTLSTransmitter,
    loggers: Loggers,
) : FilesRemotes {
    private val logger = loggers.create("[Files]")

    override fun getFile(id: UUID): ByteArray {
        logger.debug("get file by id: $id")
        return tlsTransmitter.execute(
            method = "POST",
            query = "/v1/files",
            encoded = id.toByteArray(),
            decode = {it},
        )
    }
}
