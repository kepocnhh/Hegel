package org.kepocnhh.hegel.entity

internal sealed interface ItemsSyncResponse {
    data object NotModified : ItemsSyncResponse
}
