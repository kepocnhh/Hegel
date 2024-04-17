package org.kepocnhh.hegel.provider

internal interface Transformer<T : Any> {
    fun encode(value: T): ByteArray
    fun decode(bytes: ByteArray): T
}
