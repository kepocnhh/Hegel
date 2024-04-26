package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class StorageInfo(
    val meta: Map<UUID, Info>,
    val deleted: Set<UUID>,
)
