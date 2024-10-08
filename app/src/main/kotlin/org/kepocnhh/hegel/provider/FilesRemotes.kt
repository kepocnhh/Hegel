package org.kepocnhh.hegel.provider

import java.util.UUID

internal interface FilesRemotes {
    fun getFile(id: UUID): ByteArray
}
