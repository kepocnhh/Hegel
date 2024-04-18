package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Meta

internal interface Remotes {
    fun itemsSync(meta: Meta): ItemsSyncResponse
    fun itemsSyncMerge(request: ItemsSyncMergeRequest)
}
