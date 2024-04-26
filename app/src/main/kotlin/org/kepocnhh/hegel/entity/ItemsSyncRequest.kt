package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncRequest(
    val storageId: UUID,
    val hash: String,
)
