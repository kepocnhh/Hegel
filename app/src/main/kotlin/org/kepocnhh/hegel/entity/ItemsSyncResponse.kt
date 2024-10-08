package org.kepocnhh.hegel.entity

import sp.kx.storages.SyncResponse
import java.util.UUID

internal data class ItemsSyncResponse(
    val sessionId: UUID,
    val delegate: SyncResponse,
)
