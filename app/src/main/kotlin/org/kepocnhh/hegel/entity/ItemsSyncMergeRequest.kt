package org.kepocnhh.hegel.entity

import sp.kx.storages.MergeInfo
import java.util.UUID

internal data class ItemsSyncMergeRequest(
    val sessionId: UUID,
    val storages: Map<UUID, MergeInfo>
)
