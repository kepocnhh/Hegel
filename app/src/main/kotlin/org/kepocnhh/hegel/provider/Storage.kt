package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Described
import java.util.UUID

internal interface Storage<T : Any> {
    val id: UUID
    val hash: String
    val items: List<Described<T>>
    val deleted: Set<UUID>

    fun delete(id: UUID)
    fun add(item: T)
    fun update(id: UUID, item: T)
    fun merge(items: List<Described<T>>, deleted: Set<UUID>) // todo bytes?
    // todo getMergeInfo
}
