package org.kepocnhh.hegel.entity

internal class FileRequest(
    val fd: FileDelegate,
    val index: Long,
    val count: Int,
) {
    override fun toString(): String {
        return "FR($fd/$index/$count)"
    }
}
