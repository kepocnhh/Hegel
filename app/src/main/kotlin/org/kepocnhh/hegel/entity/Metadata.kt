package org.kepocnhh.hegel.entity

import kotlin.time.Duration

internal data class Metadata(
    val created: Duration,
    val updated: Duration,
    val hash: String,
)
