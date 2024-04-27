package org.kepocnhh.hegel.entity

import java.util.UUID

internal data class Bar(
    val count: Int,
) {
    companion object {
        val STORAGE_ID = UUID.fromString("6c7a0b49-89e9-45ee-945c-0faad06a3df7")
    }
}
