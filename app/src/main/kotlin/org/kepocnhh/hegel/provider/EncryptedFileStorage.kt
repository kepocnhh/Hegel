package org.kepocnhh.hegel.provider

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import org.kepocnhh.hegel.BuildConfig
import sp.kx.storages.SyncStreamsStorage
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal class EncryptedFileStorage<T : Any>(
    id: UUID,
    private val context: Context,
    private val transformer: Transformer<T>,
) : SyncStreamsStorage<T>(
    id = id,
) {
    init {
        if (!file().exists()) {
            encrypted().openFileOutput().use { it.write("\n0".toByteArray()) }
        }
    }

    private fun file(): File {
        return context.filesDir.resolve(id.toString())
    }

    private fun encrypted(): EncryptedFile {
        return EncryptedFile.Builder(
            file(),
            context,
            MasterKeys.getOrCreate(getSpec()),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
    }

    private val md = MessageDigest.getInstance("MD5") // todo

    override fun now(): Duration {
        return System.currentTimeMillis().milliseconds // todo
    }

    override fun randomUUID(): UUID {
        return UUID.randomUUID() // todo
    }

    override fun decode(bytes: ByteArray): T {
        return transformer.decode(bytes)
    }

    override fun encode(item: T): ByteArray {
        return transformer.encode(item)
    }

    override fun hash(bytes: ByteArray): String {
        return BigInteger(1, md.digest(bytes)).toString(16)
    }

    override fun inputStream(): InputStream {
        return encrypted().openFileInput()
    }

    override fun outputStream(): OutputStream {
        return encrypted().openFileOutput()
    }

    companion object {
        private const val KEY_ALIAS = BuildConfig.APPLICATION_ID + ":encrypted:sync:storage"
        private const val KEY_SIZE = 256

        private fun getSpec(): KeyGenParameterSpec {
            val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val blocks = KeyProperties.BLOCK_MODE_GCM
            val paddings = KeyProperties.ENCRYPTION_PADDING_NONE
            return KeyGenParameterSpec
                .Builder(KEY_ALIAS, purposes)
                .setBlockModes(blocks)
                .setEncryptionPaddings(paddings)
                .setKeySize(KEY_SIZE)
                .build()
        }
    }
}
