package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class DescribedItem<T : Any>(
    val id: UUID,
    val metadata: Metadata,
    val item: T,
)
