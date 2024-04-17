package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Meta

internal interface Serializer {
    val meta: Transformer<Meta>
}
