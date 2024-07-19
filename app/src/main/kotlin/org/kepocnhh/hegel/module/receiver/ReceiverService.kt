package org.kepocnhh.hegel.module.receiver

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.util.http.HttpRequest
import org.kepocnhh.hegel.util.http.HttpResponse
import org.kepocnhh.hegel.util.http.HttpService
import org.kepocnhh.hegel.util.toHEX
import sp.kx.storages.CommitInfo
import sp.kx.storages.Described
import sp.kx.storages.SyncInfo
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class ReceiverService : HttpService(_state) {
    private val logger = App.injection.loggers.create("[Receiver]")
    private val routing = mapOf(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
        "/v1/items/merge" to mapOf(
            "POST" to ::onPostItemsMerge,
        ),
    )

    private fun onItemsMerge(request: ItemsSyncMergeRequest): HttpResponse {
        val oldSession = App.injection.locals.session
        if (oldSession == null) {
            return HttpResponse(
                code = 500,
                message = "TODO", // todo
            )
        }
        if (oldSession.id != request.sessionId) {
            return HttpResponse(
                code = 500,
                message = "TODO", // todo
            )
        }
        if (oldSession.expires < System.currentTimeMillis().milliseconds) {
            App.injection.locals.session = null
            return HttpResponse(
                code = 500,
                message = "TODO", // todo
            )
        }
        val commits = App.injection.storages.merge(infos = request.merges)
        val response = ItemsSyncMergeResponse(commits = commits)
        App.injection.locals.session = null
        val body = App.injection.serializer.remote.mergeResponse.encode(response)
        return HttpResponse(
            code = 200,
            message = "OK",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to body.size.toString(),
            ),
            body = body,
        )
    }

    private fun onPostItemsMerge(request: HttpRequest): HttpResponse {
        logger.debug("on post items merge...")
        val bytes = request.body ?: TODO()
        return onItemsMerge(App.injection.serializer.remote.syncMerge.decode(bytes))
    }

    private fun onItemsSync(hashes: Map<UUID, ByteArray>): HttpResponse {
        val oldSession = App.injection.locals.session
        if (oldSession != null) {
            if (oldSession.expires > System.currentTimeMillis().milliseconds) {
                return HttpResponse(
                    code = 500,
                    message = "TODO", // todo
                )
            } else {
                App.injection.locals.session = null
            }
        }
        val syncs = App.injection.storages.getSyncInfo(hashes)
        if (syncs.isEmpty()) {
            logger.debug("not modified")
            return HttpResponse(
                code = 304,
                message = "Not Modified",
            )
        }
        val session = Session(
            id = UUID.randomUUID(),
            expires = System.currentTimeMillis().milliseconds + 1.minutes,
        )
        App.injection.locals.session = session
        logger.debug("syncs: ${syncs.mapValues { (_, si) -> si.infos.mapValues { (_, ii) -> ii.hash.toHEX() } }}") // todo
        val response = ItemsSyncResponse(
            sessionId = session.id,
            syncs = syncs,
        )
        val body = App.injection.serializer.remote.syncResponse.encode(response)
        return HttpResponse(
            code = 200,
            message = "OK",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to body.size.toString(),
            ),
            body = body,
        )
    }

    private fun onPostItemsSync(request: HttpRequest): HttpResponse {
        logger.debug("on post items sync...")
        val bytes = request.body ?: TODO()
        val syncRequest = App.injection.serializer.remote.syncRequest.decode(bytes)
        return onItemsSync(hashes = syncRequest.hashes)
    }

    override fun onSocketAccept(request: HttpRequest): HttpResponse {
        val route = routing[request.query] ?: return HttpResponse(
            code = 404,
            message = "No Found",
        )
        val transform = route[request.method] ?: return HttpResponse(
            code = 405,
            message = "Method Not Allowed",
        )
        return transform(request)
    }

    companion object {
        private val _state = MutableStateFlow<State>(State.Stopped)
        val state = _state.asStateFlow()
    }
}
