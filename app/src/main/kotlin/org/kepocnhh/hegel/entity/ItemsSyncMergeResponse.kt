package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncMergeResponse(
    val storages: Map<UUID, List<Described<ByteArray>>>,
)
