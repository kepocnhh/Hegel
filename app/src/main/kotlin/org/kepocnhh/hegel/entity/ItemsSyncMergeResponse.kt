package org.kepocnhh.hegel.entity

import sp.kx.storages.Described
import java.util.UUID

internal data class ItemsSyncMergeResponse(
    val storages: Map<UUID, List<Described<ByteArray>>>,
)
