package org.kepocnhh.hegel.module.receiver

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kepocnhh.hegel.util.http.HttpRequest
import org.kepocnhh.hegel.util.http.HttpResponse
import org.kepocnhh.hegel.util.http.HttpService

internal class ReceiverService : HttpService(_state) {
    override fun onSocketAccept(request: HttpRequest): HttpResponse {
        TODO("Not yet implemented: onSocketAccept")
    }

    companion object {
        private val _state = MutableStateFlow<State>(State.Stopped)
        val state = _state.asStateFlow()
    }
}
