package org.kepocnhh.hegel.entity

import java.util.UUID
import kotlin.time.Duration

internal data class Session(
    val id: UUID,
    val expires: Duration,
)
