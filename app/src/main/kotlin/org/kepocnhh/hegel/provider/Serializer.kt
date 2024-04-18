package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Meta
import java.util.UUID

internal interface Serializer {
    val meta: ListTransformer<Meta>
    val uuid: ListTransformer<UUID>
    val needUpdate: Transformer<ItemsSyncResponse.NeedUpdate>
    val foo: ListTransformer<Foo>
    val syncMerge: Transformer<ItemsSyncMergeRequest>
    val mergeResponse: Transformer<ItemsSyncMergeResponse>
}
