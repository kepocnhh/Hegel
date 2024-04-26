package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class Described<T : Any>(
    val id: UUID,
    val info: Info,
    val item: T,
)
