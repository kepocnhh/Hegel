package org.kepocnhh.hegel.provider

import sp.kx.storages.HashFunction
import java.security.MessageDigest

internal class MD5HashFunction : HashFunction {
    private val md = MessageDigest.getInstance("MD5")

    override val size = 16

    override fun map(bytes: ByteArray): ByteArray {
        return md.digest(bytes)
    }
}
