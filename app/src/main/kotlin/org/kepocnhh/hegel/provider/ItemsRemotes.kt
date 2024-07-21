package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse

internal interface ItemsRemotes {
    fun sync(request: ItemsSyncRequest): ItemsSyncResponse
    fun merge(request: ItemsMergeRequest): ItemsMergeResponse
}
