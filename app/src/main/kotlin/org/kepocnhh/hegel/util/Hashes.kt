package org.kepocnhh.hegel.util

import sp.kx.bytes.readInt
import sp.kx.bytes.readUUID
import sp.kx.bytes.write
import sp.kx.storages.HashFunction
import java.util.HashMap
import java.util.UUID

object Hashes {
    fun toByteArray(hashes: Map<UUID, ByteArray>, hf: HashFunction): ByteArray {
        val entries = hashes.entries.toList()
        val size = entries.size
        val encoded = ByteArray(4 + size * (16 + hf.size))
        encoded.write(value = size)
        for (index in 0 until size) {
            val (id, hash) = entries[index]
            encoded.write(index = 4 + index * (16 + hf.size), value = id)
            System.arraycopy(hash, 0, encoded, 4 + index * (16 + hf.size) + 16, hash.size)
        }
        return encoded
    }

    fun fromByteArray(encoded: ByteArray, hf: HashFunction): Map<UUID, ByteArray> {
        val size = encoded.readInt()
        val hashes = HashMap<UUID, ByteArray>(size)
        for (index in 0 until size) {
            val id = encoded.readUUID(index = 4 + index * (16 + hf.size))
            val hash = ByteArray(hf.size)
            System.arraycopy(encoded, 4 + index * (16 + hf.size) + 16, hash, 0, hf.size)
            hashes[id] = hash
        }
        return hashes
    }
}
