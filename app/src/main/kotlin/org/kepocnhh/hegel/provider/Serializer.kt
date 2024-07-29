package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import sp.kx.storages.Transformer

internal interface Serializer {
    interface Remote {
        val syncRequest: Transformer<ItemsSyncRequest>
        val syncResponse: Transformer<ItemsSyncResponse>
        val mergeRequest: Transformer<ItemsMergeRequest>
        val mergeResponse: Transformer<ItemsMergeResponse>
    }

    val foo: Transformer<Foo>
    val bar: Transformer<Bar>
    val remote: Remote
}
