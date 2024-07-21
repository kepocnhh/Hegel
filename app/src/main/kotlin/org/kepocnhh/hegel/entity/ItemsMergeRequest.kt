package org.kepocnhh.hegel.entity

import sp.kx.storages.MergeInfo
import java.util.UUID

internal data class ItemsMergeRequest(
    val sessionId: UUID,
    val merges: Map<UUID, MergeInfo>
)
