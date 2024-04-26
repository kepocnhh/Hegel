package org.kepocnhh.hegel.module.transmitter

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import java.util.UUID

internal class TransmitterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    private val logger = injection.loggers.create("[Transmitter]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()

    private suspend fun onSyncMerge(response: ItemsSyncMergeResponse, deleted: Set<UUID>) {
        logger.debug("sync merge...")
        withContext(injection.contexts.default) {
            injection.locals.foo.merge(response.items, deleted)
        }
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.items
        } // todo
        _state.value = State(loading = false)
    }

    private suspend fun onSyncMerge(result: Result<ItemsSyncMergeResponse>, deleted: Set<UUID>) {
        if (result.isFailure) {
            logger.warning("sync merge: " + result.exceptionOrNull())
            _state.value = State(loading = false)
            return
        }
        onSyncMerge(result.getOrThrow(), deleted = deleted)
    }

    private suspend fun onNeedUpdate(response: ItemsSyncResponse.NeedUpdate) {
        logger.debug("need update...")
        val download = mutableSetOf<UUID>()
        val items = mutableListOf<Described<Foo>>()
        withContext(injection.contexts.default) {
            for (described in injection.locals.foo.items) {
                if (response.info.containsKey(described.id)) continue
                if (response.deleted.contains(described.id)) continue
                items.add(described)
            }
            for ((id, info) in response.info) {
                val described = injection.locals.foo.items.firstOrNull { it.id == id }
                if (described == null) {
                    if (injection.locals.foo.deleted.contains(id)) continue
                    download.add(id)
                } else if (info.hash != described.info.hash) {
                    if (info.updated > described.info.updated) {
                        download.add(id)
                    } else {
                        items.add(described)
                    }
                }
            }
        }
        val result = withContext(injection.contexts.default) {
            runCatching {
                val request = ItemsSyncMergeRequest(
                    download = download,
                    items = items,
                    deleted = injection.locals.foo.deleted,
                )
                logger.debug("upload: " + request.items.map { it.id })
                injection.remotes.itemsSyncMerge(request)
            }
        }
        onSyncMerge(result, deleted = response.deleted)
    }

    private suspend fun onResponse(response: ItemsSyncResponse) {
        when (response) {
            ItemsSyncResponse.NotModified -> {
                _state.value = State(loading = false)
                return
            }
            is ItemsSyncResponse.NeedUpdate -> {
                onNeedUpdate(response)
            }
        }
    }

    private suspend fun onResponse(result: Result<ItemsSyncResponse>) {
        if (result.isFailure) {
            logger.warning("sync items: " + result.exceptionOrNull())
            _state.value = State(loading = false)
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
                    storageId = Foo.STORAGE_ID,
                    hash = injection.locals.foo.hash,
                )
                injection.remotes.itemsSync(request)
            }
        }
        onResponse(result)
    }
}
