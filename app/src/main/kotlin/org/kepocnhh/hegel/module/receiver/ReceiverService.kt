package org.kepocnhh.hegel.module.receiver

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.provider.Storage
import org.kepocnhh.hegel.util.http.HttpRequest
import org.kepocnhh.hegel.util.http.HttpResponse
import org.kepocnhh.hegel.util.http.HttpService
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class ReceiverService : HttpService(_state) {
    private val logger = App.injection.loggers.create("[Receiver]")
    private val routing = mapOf(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
        "/v1/items/sync/merge" to mapOf(
            "POST" to ::onPostItemsSyncMerge,
        ),
    )

    private fun onSyncMerge(request: ItemsSyncMergeRequest): HttpResponse {
        val oldSession = App.injection.locals.session
        if (oldSession == null) {
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
        logger.debug("receive: " + request.items.map { it.id })
        val response = ItemsSyncMergeResponse(
            items = App.injection.locals.foo.items.filter { request.download.contains(it.id) }
        )
        App.injection.locals.foo.merge(items = request.items, deleted = request.deleted)
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

    private fun onPostItemsSyncMerge(request: HttpRequest): HttpResponse {
        logger.debug("on post items sync merge...")
        val bytes = request.body ?: TODO()
        return onSyncMerge(App.injection.serializer.remote.syncMerge.decode(bytes))
    }

    private fun onMetaSync(hashes: Map<UUID, String>): HttpResponse {
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
        val storages = mutableMapOf<UUID, ItemsSyncResponse.NeedUpdate.StorageInfo>()
        for ((id, hash) in hashes) {
            val storage = when (id) {
                Foo.STORAGE_ID -> App.injection.locals.foo
                else -> TODO()
            }
            if (storage.hash == hash) continue
            storages[id] = ItemsSyncResponse.NeedUpdate.StorageInfo(
                info = storage.items.associate { it.id to it.info },
                deleted = storage.deleted,
            )
        }
        if (storages.isEmpty()) {
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
        val response = ItemsSyncResponse.NeedUpdate(
            sessionId = session.id,
            storages = storages,
        )
        val body = App.injection.serializer.remote.needUpdate.encode(response)
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
        return onMetaSync(hashes = syncRequest.hashes)
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
