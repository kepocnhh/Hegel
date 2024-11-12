package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.FileRequest
import java.io.File

/*
internal class FinalBytesLoader : BytesLoader<String>() {
    private class FileBytesWrapper(name: String) : BytesWrapper {
        private val tmp: File = App.injection.dirs.cache.resolve(name)
        private val dst: File = App.injection.dirs.files.resolve(name)
        override fun clear() {
            tmp.delete()
        }

        override fun size(): Long {
            return tmp.length()
        }

        override fun append(bytes: ByteArray) {
            tmp.appendBytes(bytes)
        }

        override fun hash(): ByteArray {
            return App.injection.secrets.hash(tmp.readBytes())
        }

        override fun commit() {
            if (dst.exists()) TODO("file: ${dst.absolutePath} exists!")
            tmp.renameTo(dst)
        }
    }

    override fun getWrapper(key: String): BytesWrapper {
        return FileBytesWrapper(name = key)
    }

    override fun getBytes(key: String, size: Long, index: Long, count: Int): ByteArray {
        val address = App.injection.locals.address ?: error("No address!")
        val request = FileRequest(
            name = key,
            size = size,
            index = index,
            count = count,
        )
        return App.injection.remotes.files(address).getBytes(request = request)
    }
}
*/
