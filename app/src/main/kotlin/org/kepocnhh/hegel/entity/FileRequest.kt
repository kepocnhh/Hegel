package org.kepocnhh.hegel.entity

import java.net.URI

internal data class FileRequest(
    val uri: URI,
    val index: Long,
    val count: Int,
)
