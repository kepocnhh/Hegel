package org.kepocnhh.hegel.entity

import java.security.PublicKey

internal class Keys(
    val publicKey: PublicKey,
    val privateKeyEncrypted: ByteArray,
)
