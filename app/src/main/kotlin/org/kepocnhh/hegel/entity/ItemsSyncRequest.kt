package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class ItemsSyncRequest(
    val id: UUID,
    val hash: String,
)
