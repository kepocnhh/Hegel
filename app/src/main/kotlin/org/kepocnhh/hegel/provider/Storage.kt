package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Meta
import java.util.UUID

internal interface Storage<T : Any> {
    val meta: Meta
    val metas: List<Meta>
    var items: List<T>
    val deleted: List<UUID>
}
