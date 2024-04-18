package org.kepocnhh.hegel.entity

import java.util.UUID
import kotlin.time.Duration

internal data class Meta(
    val id: UUID,
    val updated: Duration,
    val hash: String,
)
