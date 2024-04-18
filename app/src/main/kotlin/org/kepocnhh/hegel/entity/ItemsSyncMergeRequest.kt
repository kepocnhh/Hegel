package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncMergeRequest(
    val download: List<UUID>,
    // upload
    val deleted: List<UUID>,
)
