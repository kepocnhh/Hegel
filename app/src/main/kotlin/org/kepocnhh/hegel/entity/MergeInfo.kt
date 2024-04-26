package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class MergeInfo<T : Any>(
    val download: Set<UUID>,
    val items: List<Described<T>>,
    val deleted: Set<UUID>,
)
