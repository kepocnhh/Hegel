package org.kepocnhh.hegel.entity

import java.util.UUID
import kotlin.time.Duration

internal data class Foo(
    val id: UUID,
    val created: Duration,
    val text: String,
)
