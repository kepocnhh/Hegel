package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse

internal interface Serializer {
    interface Remote {
        val syncRequest: Transformer<ItemsSyncRequest>
        val needUpdate: Transformer<ItemsSyncResponse.NeedUpdate>
        val syncMerge: Transformer<ItemsSyncMergeRequest>
        val mergeResponse: Transformer<ItemsSyncMergeResponse>
    }

    val foo: ListTransformer<Described<Foo>>
    val remote: Remote
}
