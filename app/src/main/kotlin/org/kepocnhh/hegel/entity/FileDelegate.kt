package org.kepocnhh.hegel.entity

import sp.kx.bytes.toHEX
import java.util.Objects

internal class FileDelegate(
    val hash: ByteArray,
    val size: Long,
) {
    fun name(): String {
        return hash.copyOf(16).toHEX()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is FileDelegate -> hash.contentEquals(other.hash) && size == other.size
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            hash.contentHashCode(),
            size,
        )
    }

    override fun toString(): String {
        return "FD($size/${hash.toHEX()})"
    }
}
