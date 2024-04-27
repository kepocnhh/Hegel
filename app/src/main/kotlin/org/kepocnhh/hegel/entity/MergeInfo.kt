package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class MergeInfo(
    val download: Set<UUID>,
    val items: List<Described<ByteArray>>,
    val deleted: Set<UUID>,
)
