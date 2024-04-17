package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Meta

internal class JsonSerializer : Serializer {
    override val meta: Transformer<Meta> = object : Transformer<Meta> {
        override fun encode(value: Meta): ByteArray {
            TODO("Not yet implemented: encode")
        }

        override fun decode(bytes: ByteArray): Meta {
            TODO("Not yet implemented: decode")
        }
    }
}
