package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Meta
import java.util.UUID

internal interface Serializer {
    interface Remote {
        val syncRequest: Transformer<ItemsSyncRequest>
        val needUpdate: Transformer<ItemsSyncResponse.NeedUpdate>
        val syncMerge: Transformer<ItemsSyncMergeRequest>
        val mergeResponse: Transformer<ItemsSyncMergeResponse>
    }

    val meta: ListTransformer<Meta>
    val uuid: ListTransformer<UUID>
    val foo: ListTransformer<Foo>
    val remote: Remote
}
