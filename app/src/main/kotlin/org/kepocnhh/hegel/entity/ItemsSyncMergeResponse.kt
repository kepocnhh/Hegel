package org.kepocnhh.hegel.entity

import sp.kx.storages.CommitInfo
import java.util.UUID

internal data class ItemsSyncMergeResponse(
    val storages: Map<UUID, CommitInfo>,
)
