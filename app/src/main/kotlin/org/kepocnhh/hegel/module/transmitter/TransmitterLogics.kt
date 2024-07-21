package org.kepocnhh.hegel.module.transmitter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.ItemsMergeRequest
import org.kepocnhh.hegel.entity.ItemsMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.NotModifiedException
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.util.toHEX
import sp.kx.logics.Logics
import java.net.URL

internal class TransmitterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Broadcast {
        class OnSync(val result: Result<Unit>) : Broadcast
        class OnAddressError(val error: Throwable) : Broadcast
    }

    data class State(
        val loading: Boolean,
    )

    data class AddressState(
        val value: URL,
    )

    private val logger = injection.loggers.create("[Transmitter]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _addressState = MutableStateFlow<AddressState?>(null)
    val addressState = _addressState.asStateFlow()
    private val _broadcast = MutableSharedFlow<Broadcast>()
    val broadcast = _broadcast.asSharedFlow()

    private suspend fun itemsMerge(response: ItemsMergeResponse) {
        logger.debug("items merge...")
        withContext(injection.contexts.default) {
            injection.storages.commit(infos = response.commits)
        }
        _state.value = State(loading = false)
        _broadcast.emit(Broadcast.OnSync(Result.success(Unit)))
    }

    private suspend fun onItemsSyncResponse(response: ItemsSyncResponse) {
        logger.debug("need update...")
        logger.debug("syncs: ${response.syncs.mapValues { (_, si) -> si.infos.mapValues { (_, ii) -> ii.hash.toHEX() } }}") // todo
        val merges = withContext(injection.contexts.default) {
            injection.storages.getMergeInfo(infos = response.syncs)
        }
        withContext(injection.contexts.default) {
            runCatching {
                val request = ItemsMergeRequest(
                    sessionId = response.sessionId,
                    merges = merges,
                )
                merges.forEach { (storageId, mergeInfo) ->
                    logger.debug("upload[$storageId]: " + mergeInfo.items.map { it.id }) // todo
                } // todo
                val url = injection.locals.address ?: TODO()
                injection.remotes.items(url = url).merge(request)
            }
        }.fold(
            onSuccess = {
                itemsMerge(response = it)
            },
            onFailure = { error ->
                logger.warning("items merge: $error")
                _state.value = State(loading = false)
                _broadcast.emit(Broadcast.OnSync(Result.failure(error)))
            },
        )
    }

    private suspend fun itemsSync(url: URL) {
        logger.debug("items sync...")
        withContext(injection.contexts.default) {
            runCatching {
                val request = ItemsSyncRequest(hashes = injection.storages.hashes())
                logger.debug("hashes: ${request.hashes.mapValues { (_, it) -> it.toHEX() }}")
                injection.remotes.items(url).sync(request)
            }
        }.fold(
            onSuccess = { response ->
                withContext(injection.contexts.default) {
                    injection.locals.address = url
                }
                onItemsSyncResponse(response)
            },
            onFailure = { error ->
                when (error) {
                    is NotModifiedException -> {
                        logger.debug("not modified")
                        _state.value = State(loading = false)
                        _broadcast.emit(Broadcast.OnSync(Result.success(Unit)))
                    }
                    else -> {
                        logger.warning("sync items: $error")
                        _state.value = State(loading = false)
                        _broadcast.emit(Broadcast.OnSync(Result.failure(error)))
                    }
                }
            },
        )
    }

    fun itemsSync(spec: String) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            runCatching {
                val url = URL(spec)
                val protocols = setOf("http")
                val protocol = url.protocol
                if (!protocols.contains(protocol)) error("Protocol \"$protocol\" is not supported!")
                url
            }.recoverCatching {
                if (spec.isEmpty()) error("Spec is empty!")
                if (spec.isBlank()) error("Spec is blank!")
                URL("http://$spec")
            }
        }.fold(
            onSuccess = { url ->
                val message = """
                    url: $url
                    protocol: ${url.protocol}
                    host: ${url.host}
                    port: ${url.port}
                    path: ${url.path}
                """.trimIndent()
                logger.debug(message) // todo
                itemsSync(url = url)
            },
            onFailure = { error ->
                logger.warning("url parse error: $error")
                _state.value = State(loading = false)
                _broadcast.emit(Broadcast.OnAddressError(error = error))
            },
        )
    }

    fun requestAddressState() = launch {
        val address = withContext(injection.contexts.default) {
            injection.locals.address
        }
        if (address != null) {
            _addressState.value = AddressState(value = address)
        }
    }
}
