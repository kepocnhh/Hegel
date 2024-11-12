package org.kepocnhh.hegel.provider

import org.kepocnhh.hegel.entity.FileRequest
import sp.kx.bytes.loader.BytesRequester
import java.net.URI

internal class FinalBytesRequester(
    private val locals: Locals,
    private val remotes: Remotes,
) : BytesRequester {
    override fun request(uri: URI, index: Long, count: Int): ByteArray {
        val address = locals.address ?: error("No address!")
        val request = FileRequest(
            uri = uri,
            index = index,
            count = count,
        )
        return remotes.files(address).getBytes(request = request)
    }
}
