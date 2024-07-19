package org.kepocnhh.hegel.entity

import sp.kx.storages.SyncInfo
import java.util.UUID

data class ItemsSyncResponse(
    val sessionId: UUID,
    val syncs: Map<UUID, SyncInfo>,
)
