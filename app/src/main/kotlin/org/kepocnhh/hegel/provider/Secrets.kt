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
    fun toSecretKey(encoded: ByteArray): SecretKey
    fun newSecretKey(): SecretKey
    fun hash(encoded: ByteArray): ByteArray
    fun encrypt(key: SecretKey, decrypted: ByteArray): ByteArray
    fun decrypt(key: SecretKey, encrypted: ByteArray): ByteArray
    fun encrypt(key: PublicKey, decrypted: ByteArray): ByteArray
    fun decrypt(key: PrivateKey, encrypted: ByteArray): ByteArray
    fun sign(key: PrivateKey, encoded: ByteArray): ByteArray
    fun verify(key: PublicKey, encoded: ByteArray, signature: ByteArray): Boolean
}
