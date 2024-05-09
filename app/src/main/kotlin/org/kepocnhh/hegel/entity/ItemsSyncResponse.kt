package org.kepocnhh.hegel.entity

import sp.kx.storages.SyncInfo
import java.util.UUID

internal sealed interface ItemsSyncResponse {
    data object NotModified : ItemsSyncResponse
    data class NeedUpdate(
        val sessionId: UUID,
        val syncs: Map<UUID, SyncInfo>,
    ) : ItemsSyncResponse
}
