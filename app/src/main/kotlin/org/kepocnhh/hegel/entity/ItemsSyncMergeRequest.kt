package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncMergeRequest(
    val sessionId: UUID,
    val storages: Map<UUID, MergeInfo>
)
