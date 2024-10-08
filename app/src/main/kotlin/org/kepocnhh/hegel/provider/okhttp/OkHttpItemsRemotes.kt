package org.kepocnhh.hegel.provider.okhttp

import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.provider.ItemsRemotes
import org.kepocnhh.hegel.provider.Loggers
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.util.toHEX

internal class OkHttpItemsRemotes(
    private val tlsTransmitter: OkHttpTLSTransmitter,
    loggers: Loggers,
    private val serializer: Serializer,
) : ItemsRemotes {
    private val logger = loggers.create("[Items]")

    override fun sync(request: ItemsSyncRequest): ItemsSyncResponse {
        val encoded = serializer.remote.syncRequest.encode(request)
        logger.debug("request decrypted: ${encoded.toHEX()}")
        return tlsTransmitter.execute(
            method = "POST",
            query = "/v1/items/sync",
            encoded = encoded,
            decode = serializer.remote.syncResponse::decode,
        )
    }

    override fun merge(request: ItemsMergeRequest): ItemsMergeResponse {
        return tlsTransmitter.execute(
            method = "POST",
            query = "/v1/items/merge",
            encoded = serializer.remote.mergeRequest.encode(request),
            decode = serializer.remote.mergeResponse::decode,
        )
    }
}
