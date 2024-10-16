package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Bar2Baz
import org.kepocnhh.hegel.entity.Baz
import org.kepocnhh.hegel.entity.FileRequest
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Pic
import sp.kx.storages.Transformer

internal interface Serializer {
    interface Remote {
        val syncRequest: Transformer<ItemsSyncRequest>
        val syncResponse: Transformer<ItemsSyncResponse>
        val mergeRequest: Transformer<ItemsMergeRequest>
        val mergeResponse: Transformer<ItemsMergeResponse>
        val fileRequest: Transformer<FileRequest>
    }

    val foo: Transformer<Foo>
    val bar: Transformer<Bar>
    val baz: Transformer<Baz>
    val bar2baz: Transformer<Bar2Baz>
    val pics: Transformer<Pic>
    val remote: Remote
}
