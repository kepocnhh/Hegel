package org.kepocnhh.hegel.provider

import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

internal interface Secrets {
    fun toPublicKey(encoded: ByteArray): PublicKey
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun toKeyStore(encoded: ByteArray, password: CharArray): KeyStore
    fun getSecretKey(password: CharArray): SecretKey
    fun hash(encoded: ByteArray): ByteArray
    fun encrypt(secretKey: SecretKey, decrypted: ByteArray): ByteArray
    fun decrypt(secretKey: SecretKey, encrypted: ByteArray): ByteArray
}
