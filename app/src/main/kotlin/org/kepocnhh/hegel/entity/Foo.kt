package org.kepocnhh.hegel.entity

import java.util.UUID
import kotlin.time.Duration

internal data class Foo(
    val id: UUID,
    val created: Duration,
    val text: String,
) {
    companion object {
        val META_ID = UUID.fromString("84e44670-d301-471b-a7ac-dfd8b1e55554")
    }
}
