package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.FileRequest

internal interface FilesRemotes {
    fun getBytes(request: FileRequest): ByteArray
}
