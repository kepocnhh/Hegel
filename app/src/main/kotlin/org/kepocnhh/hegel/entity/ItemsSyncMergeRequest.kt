package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncMergeRequest(
    val download: Set<UUID>,
    val items: List<Described<Foo>>,
    val deleted: Set<UUID>,
)
