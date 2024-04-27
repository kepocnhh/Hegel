package org.kepocnhh.hegel.entity

import java.util.Objects
import java.util.UUID

internal class Described<T : Any>(
    val id: UUID,
    val info: Info,
    val item: T,
) {
    fun copy(
        info: Info,
        item: T,
    ): Described<T> {
        return Described(
            id = id,
            info = info,
            item = item,
        )
    }

    override fun toString(): String {
        return "{id: $id, info: $info, item: ${item::class.java}}"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Described<*> -> {
                other.id == id && other.info == info && other.item == item
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id,
            info,
            item,
        )
    }
}

internal fun <T : Any, U : Any> Described<T>.map(transform: (T) -> U): Described<U> {
    return Described(
        id = id,
        info = info,
        item = transform(item),
    )
}
