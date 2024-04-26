package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class Foo(
    val text: String,
) {
    companion object {
        val STORAGE_ID = UUID.fromString("84e44670-d301-471b-a7ac-dfd8b1e55554")
    }
}
