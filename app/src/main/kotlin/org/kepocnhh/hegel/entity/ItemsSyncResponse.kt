package org.kepocnhh.hegel.entity

import java.util.UUID

internal sealed interface ItemsSyncResponse {
    data object NotModified : ItemsSyncResponse
    data class NeedUpdate(
        val sessionId: UUID,
        val storages: Map<UUID, StorageInfo>,
    ) : ItemsSyncResponse {
        data class StorageInfo(
            val info: Map<UUID, Info>,
            val deleted: Set<UUID>,
        )
    }
}
