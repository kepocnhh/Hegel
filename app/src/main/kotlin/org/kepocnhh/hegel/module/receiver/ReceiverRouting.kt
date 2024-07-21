package org.kepocnhh.hegel.module.receiver

import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.util.toHEX
import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class ReceiverRouting(
    private val injection: Injection,
) : HttpRouting {
    private val logger = injection.loggers.create("[Receiver|Routing]")
    private val mapping = mapOf(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
        "/v1/items/merge" to mapOf(
            "POST" to ::onPostItemsMerge,
        ),
    )

    private fun onPostItemsSync(request: HttpRequest): HttpResponse {
        logger.debug("on post items sync...")
        val requestBody = request.body ?: return HttpResponse(
            version = "1.1",
            code = 500,
            message = "Internal Server Error",
            headers = emptyMap(),
            body = "todo".toByteArray(),
        )
        val syncRequest = injection.serializer.remote.syncRequest.decode(requestBody)
        val oldSession = injection.locals.session
        val now = System.currentTimeMillis().milliseconds
        if (oldSession != null) {
            if (oldSession.expires > now) {
                return HttpResponse(
                    version = "1.1",
                    code = 500,
                    message = "Internal Server Error",
                    headers = emptyMap(),
                    body = "todo".toByteArray(),
                )
            }
            injection.locals.session = null
        }
        val syncs = injection.storages.getSyncInfo(syncRequest.hashes)
        if (syncs.isEmpty()) {
            logger.debug("not modified")
            return HttpResponse(
                version = "1.1",
                code = 304,
                message = "Not Modified",
                headers = emptyMap(),
                body = null,
            )
        }
        val session = Session(
            id = UUID.randomUUID(),
            expires = now + 1.minutes,
        )
        injection.locals.session = session
        logger.debug("syncs: ${syncs.mapValues { (_, si) -> si.infos.mapValues { (_, ii) -> ii.hash.toHEX() } }}") // todo
        val response = ItemsSyncResponse(
            sessionId = session.id,
            syncs = syncs,
        )
        val responseBody = injection.serializer.remote.syncResponse.encode(response)
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to responseBody.size.toString(),
            ),
            body = responseBody,
        )
    }

    private fun onPostItemsMerge(request: HttpRequest): HttpResponse {
        logger.debug("on post items merge...")
        val requestBody = request.body ?: return HttpResponse(
            version = "1.1",
            code = 500,
            message = "Internal Server Error",
            headers = emptyMap(),
            body = "todo".toByteArray(),
        )
        val mergeRequest = injection.serializer.remote.mergeRequest.decode(requestBody)
        val oldSession = injection.locals.session
        val now = System.currentTimeMillis().milliseconds
        if (oldSession == null) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf("message" to "No session!"),
                body = "todo".toByteArray(),
            )
        }
        if (oldSession.id != mergeRequest.sessionId) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = emptyMap(),
                body = "todo".toByteArray(),
            )
        }
        if (oldSession.expires < now) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf("message" to "Session expired!"),
                body = "todo".toByteArray(),
            )
        }
        val commits = injection.storages.merge(infos = mergeRequest.merges)
        val mergeResponse = ItemsMergeResponse(commits = commits)
        injection.locals.session = null
        val responseBody = injection.serializer.remote.mergeResponse.encode(mergeResponse)
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to responseBody.size.toString(),
            ),
            body = responseBody,
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
            null -> HttpResponse(
                version = "1.1",
                code = 404,
                message = "Not Found",
                headers = emptyMap(),
                body = null,
            )
            else -> when (val transform = route[request.method]) {
                null -> HttpResponse(
                    version = "1.1",
                    code = 405,
                    message = "Method Not Allowed",
                    headers = emptyMap(),
                    body = null,
                )
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
