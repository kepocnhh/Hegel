package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse

internal interface ItemsRemotes {
    fun sync(request: ItemsSyncRequest): ItemsSyncResponse
    fun merge(request: ItemsSyncMergeRequest): ItemsSyncMergeResponse
}
