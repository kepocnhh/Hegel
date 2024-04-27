package org.kepocnhh.hegel.entity

import java.util.Date
import java.util.Objects
import kotlin.time.Duration

internal class Info(
    val created: Duration,
    val updated: Duration,
    val hash: String,
) {
    fun copy(
        updated: Duration,
        hash: String,
    ): Info {
        return Info(
            created = created,
            updated = updated,
            hash = hash,
        )
    }

    override fun toString(): String {
        return "Info(created: ${Date(created.inWholeMilliseconds)}, updated: ${Date(updated.inWholeMilliseconds)}, hash: \"$hash\")"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Info -> other.created == created && other.updated == updated && other.hash == hash
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            created,
            updated,
            hash,
        )
    }
}
