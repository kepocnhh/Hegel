package org.kepocnhh.hegel.provider

import sp.kx.bytes.loader.BytesWrapper
import java.io.File
import java.net.URI

internal class FinalBytesWrapper(
    private val tmp: File,
    private val dst: File,
    private val secrets: Secrets,
) : BytesWrapper {
    class Factory(
        private val dirs: Dirs,
        private val secrets: Secrets,
    ) : BytesWrapper.Factory {
        override fun build(uri: URI): BytesWrapper {
            return FinalBytesWrapper(
                tmp = dirs.cache.resolve(uri.toString()),
                dst = dirs.files.resolve(uri.toString()),
                secrets = secrets,
            )
        }
    }

    override fun append(bytes: ByteArray) {
        tmp.appendBytes(bytes)
    }

    override fun check(loaded: Long) {
        if (loaded == 0L) {
            tmp.delete()
        } else {
            val length = tmp.length()
            if (length != loaded) error("Length: $length, but loaded: $loaded!")
        }
    }

    override fun commit(hash: ByteArray) {
        if (!hash.contentEquals(secrets.hash(tmp.readBytes()))) error("Hashes error!")
        if (dst.exists()) error("File: ${dst.absolutePath} exists!")
        tmp.renameTo(dst)
    }
}
