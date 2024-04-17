package org.kepocnhh.hegel.module.receiver

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Meta
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.util.http.HttpRequest
import org.kepocnhh.hegel.util.http.HttpResponse
import org.kepocnhh.hegel.util.http.HttpService

internal class ReceiverService : HttpService(_state) {
    private val serializer: Serializer = App.serializer
    private val routing = mapOf(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
    )

    private fun onFooMetaSync(meta: Meta): HttpResponse {
        TODO()
    }

    private fun onPostItemsSync(request: HttpRequest): HttpResponse {
        val bytes = request.body ?: TODO()
        val meta = serializer.meta.decode(bytes)
        return when (meta.id) {
            Foo.META_ID -> onFooMetaSync(meta)
            else -> TODO()
        }
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
