package org.kepocnhh.hegel.entity

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
}

internal fun <T : Any, U : Any> Described<T>.map(transform: (T) -> U): Described<U> {
    return Described(
        id = id,
        info = info,
        item = transform(item),
    )
}
