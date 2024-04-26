package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncRequest(
    val hashes: Map<UUID, String>,
)
