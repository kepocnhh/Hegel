package org.kepocnhh.hegel.entity

import sp.kx.storages.MergeInfo
import sp.kx.storages.SyncSession
import java.util.UUID

internal data class ItemsMergeRequest(
    val sessionId: UUID,
    val syncSession: SyncSession,
    val merges: Map<UUID, MergeInfo>
)
