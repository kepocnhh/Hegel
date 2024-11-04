package org.kepocnhh.hegel.entity

internal data class FileRequest(
    val name: String,
    val size: Long,
    val index: Long,
    val count: Int,
)
