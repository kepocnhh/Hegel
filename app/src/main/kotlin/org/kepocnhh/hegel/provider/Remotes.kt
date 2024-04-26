package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse

internal interface Remotes {
    fun itemsSync(request: ItemsSyncRequest): ItemsSyncResponse
    fun itemsSyncMerge(request: ItemsSyncMergeRequest): ItemsSyncMergeResponse
}
