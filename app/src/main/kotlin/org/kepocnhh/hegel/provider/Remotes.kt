package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.ItemsSyncResponse

internal interface Remotes {
    fun itemsSync(): ItemsSyncResponse
}
