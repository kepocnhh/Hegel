package org.kepocnhh.hegel.module.transmitter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import sp.kx.storages.MergeInfo
import java.util.UUID

internal class TransmitterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Broadcast {
        class OnSync(val result: Result<Unit>) : Broadcast
    }

    data class State(
        val loading: Boolean,
    )

    private val logger = injection.loggers.create("[Transmitter]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _broadcast = MutableSharedFlow<Broadcast>()
    val broadcast = _broadcast.asSharedFlow()

    private suspend fun onSyncMerge(response: ItemsSyncMergeResponse) {
        logger.debug("sync merge...")
        withContext(injection.contexts.default) {
            for ((storageId, info) in response.storages) {
                val storage = injection.storages.require(id = storageId)
                storage.merge(info)
            }
        }
        _state.value = State(loading = false)
        _broadcast.emit(Broadcast.OnSync(Result.success(Unit)))
    }

    private suspend fun onSyncMerge(result: Result<ItemsSyncMergeResponse>) {
        if (result.isFailure) {
            val error = result.exceptionOrNull() ?: TODO()
            logger.warning("sync merge: $error")
            _state.value = State(loading = false)
            _broadcast.emit(Broadcast.OnSync(Result.failure(error)))
            return
        }
        onSyncMerge(result.getOrThrow())
    }

    private suspend fun onNeedUpdate(response: ItemsSyncResponse.NeedUpdate) {
        logger.debug("need update...")
        logger.debug("storages: ${response.storages.map { (storageId, info) -> storageId to info.meta.map { (id, i) -> id to i.hash } }}")
        val storages = mutableMapOf<UUID, MergeInfo>()
        withContext(injection.contexts.default) {
            for ((storageId, syncInfo) in response.storages) {
                val mergeInfo = injection.storages.require(id = storageId).getMergeInfo(syncInfo)
                storages[storageId] = mergeInfo
            }
        }
        val result = withContext(injection.contexts.default) {
            runCatching {
                val request = ItemsSyncMergeRequest(
                    sessionId = response.sessionId,
                    storages = storages,
                )
                storages.forEach { (storageId, mergeInfo) ->
                    logger.debug("upload[$storageId]: " + mergeInfo.items.map { it.id }) // todo
                }
                injection.remotes.itemsSyncMerge(request)
            }
        }
        onSyncMerge(result)
    }

    private suspend fun onResponse(response: ItemsSyncResponse) {
        when (response) {
            ItemsSyncResponse.NotModified -> {
                logger.debug("not modified")
                _state.value = State(loading = false)
                _broadcast.emit(Broadcast.OnSync(Result.success(Unit)))
                return
            }
            is ItemsSyncResponse.NeedUpdate -> {
                onNeedUpdate(response)
            }
        }
    }

    private suspend fun onResponse(result: Result<ItemsSyncResponse>) {
        if (result.isFailure) {
            val error = result.exceptionOrNull() ?: TODO()
            logger.warning("sync items: $error")
            _state.value = State(loading = false)
            _broadcast.emit(Broadcast.OnSync(Result.failure(error)))
            return
        }
        onResponse(result.getOrThrow())
    }

    fun syncItems() = launch {
        logger.debug("sync items...")
        _state.value = State(loading = true)
        val result = withContext(injection.contexts.default) {
            runCatching {
                val request = ItemsSyncRequest(
                    hashes = setOf(
                        injection.storages.require<Foo>(), // todo
                        injection.storages.require<Bar>(), // todo
                    ).associate { it.id to it.hash },
                )
                logger.debug("hashes: ${request.hashes}")
                injection.remotes.itemsSync(request)
            }
        }
        onResponse(result)
    }
}
