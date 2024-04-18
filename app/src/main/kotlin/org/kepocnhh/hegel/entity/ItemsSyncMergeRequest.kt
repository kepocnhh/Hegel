package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncMergeRequest(
    val download: List<UUID>,
    val items: List<Foo>,
    val deleted: List<UUID>,
)
