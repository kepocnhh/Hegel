package org.kepocnhh.hegel.module.receiver

import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.bytes.toHEX
import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.TLSResponse
import sp.kx.http.TLSRouting
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class ReceiverRouting(
    private val injection: Injection,
) : TLSRouting(injection.tls) {
    private val logger = injection.loggers.create("[Receiver|Routing]")
    private val mapping = mapOf<String, Map<String, (HttpRequest) -> HttpResponse>>(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
        "/v1/items/merge" to mapOf(
            "POST" to ::onPostItemsMerge,
        ),
        "/v1/bytes" to mapOf(
            "POST" to ::onPostBytes,
        ),
    )

    override var requested: Map<UUID, Duration>
        get() {
            return injection.locals.requested
        }
        set(value) {
            injection.locals.requested = value
        }

    private fun onPostBytes(request: HttpRequest): HttpResponse {
        logger.debug("on post bytes...")
        return map(request) { decrypted ->
            val req = injection.serializer.remote.fileRequest.decode(decrypted)
            val index = req.index
            val count = req.count
            val fd = req.fd
            logger.debug("get bytes($index/$count) by: $fd")
            val file = injection.dirs.files.resolve(fd.name())
            if (!file.exists()) {
                TLSResponse.NotFound()
            } else {
                val size = file.length()
                if (fd.size != size) TODO("Size: $size, but fd:size: ${fd.size}!")
                val bytes = ByteArray(kotlin.math.min(count, (size - index).toInt()))
                logger.debug("try read ${bytes.size} bytes")
                file.inputStream().use {
                    it.skip(index)
                    it.read(bytes)
                }
                TLSResponse.OK(encoded = bytes)
            }
        }
    }

    private fun onPostItemsSync(request: HttpRequest): HttpResponse {
        logger.debug("on post items sync...")
        return map(request) {
            logger.debug("request decrypted: ${it.toHEX()}")
            val syncRequest = injection.serializer.remote.syncRequest.decode(it)
            onPostItemsSync(syncRequest = syncRequest)
        }
    }

    private fun onPostItemsSync(syncRequest: ItemsSyncRequest): TLSResponse {
        val oldSession = injection.locals.session
        val now = System.currentTimeMillis().milliseconds
        if (oldSession != null) {
            if (oldSession.expires > now) {
                return TLSResponse(
                    code = 500,
                    message = "Internal Server Error",
                    encoded = "todo".toByteArray(),
                )
            }
            injection.locals.session = null
        }
        val syncs = injection.storages.getSyncInfo(syncRequest.hashes)
        if (syncs.infos.isEmpty()) {
            logger.debug("not modified")
            return TLSResponse.NotModified()
        }
        val session = Session(
            id = UUID.randomUUID(),
            expires = now + 1.minutes,
        )
        injection.locals.session = session
        logger.debug("syncs: ${syncs.infos.mapValues { (_, si) -> si.infos.mapValues { (_, ii) -> ii.hash.toHEX() } }}") // todo
        val response = ItemsSyncResponse(
            sessionId = session.id,
            delegate = syncs,
        )
        return TLSResponse.OK(
            encoded = injection.serializer.remote.syncResponse.encode(response),
        )
    }

    private fun onPostItemsMerge(request: HttpRequest): HttpResponse {
        logger.debug("on post items merge...")
        return map(request) {
            val mergeRequest = injection.serializer.remote.mergeRequest.decode(it)
            onPostItemsMerge(mergeRequest = mergeRequest)
        }
    }

    private fun onPostItemsMerge(mergeRequest: ItemsMergeRequest): TLSResponse {
        val oldSession = injection.locals.session
        val now = System.currentTimeMillis().milliseconds
        if (oldSession == null) {
            return TLSResponse(
                code = 500,
                message = "Internal Server Error",
                encoded = "todo".toByteArray(),
            )
        }
        if (oldSession.id != mergeRequest.sessionId) {
            return TLSResponse(
                code = 500,
                message = "Internal Server Error",
                encoded = "todo".toByteArray(),
            )
        }
        if (oldSession.expires < now) {
            return TLSResponse(
                code = 500,
                message = "Internal Server Error",
                encoded = "todo".toByteArray(),
            )
        }
        val commits = injection.storages.merge(session = mergeRequest.syncSession, infos = mergeRequest.merges)
        val mergeResponse = ItemsMergeResponse(commits = commits)
        injection.locals.session = null
        return TLSResponse.OK(
            encoded = injection.serializer.remote.mergeResponse.encode(mergeResponse),
        )
    }

    override fun route(request: HttpRequest): HttpResponse {
        logger.debug(
            message = """
                <-- request
                ${request.method} ${request.query}
                headers: ${request.headers}
            """.trimIndent(),
        )
        val response = when (val route = mapping[request.query]) {
            null -> HttpResponse.NotFound()
            else -> when (val transform = route[request.method]) {
                null -> HttpResponse.MethodNotAllowed()
                else -> transform(request)
            }
        }
        logger.debug(
            message = """
                --> response
                ${response.code} ${response.message}
                headers: ${response.headers}
            """.trimIndent(),
        )
        return response
    }
}
