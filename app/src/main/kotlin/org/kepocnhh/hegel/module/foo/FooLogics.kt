package org.kepocnhh.hegel.module.foo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Meta
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class FooLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
        val items: List<Foo>,
    )

    private val logger = injection.loggers.create("[Foo]")
    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    fun requestItems() = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.items
        }
        _state.emit(State(loading = false, items = items))
    }

    fun deleteItem(id: UUID) = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.items.toMutableList().also { list ->
                list.removeIf { it.id == id }
                injection.locals.foo.items = list
            }
        }
        _state.emit(State(loading = false, items = items))
    }

    fun addItem(text: String) = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.items.toMutableList().also { list ->
                list += Foo(
                    id = UUID.randomUUID(),
                    created = System.currentTimeMillis().milliseconds,
                    text = text,
                )
                injection.locals.foo.items = list
            }
        }
        _state.emit(State(loading = false, items = items))
    }

    fun updateItem(id: UUID, text: String) = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.items.toMutableList().also { list ->
                val oldItem = list.firstOrNull { it.id == id } ?: TODO()
                list.removeIf { it.id == id }
                list += oldItem.copy(text = text)
                injection.locals.foo.items = list
            }
        }
        _state.emit(State(loading = false, items = items))
    }

    private suspend fun onSyncMerge(response: ItemsSyncMergeResponse, deleted: List<UUID>) {
        logger.debug("sync merge...")
        withContext(injection.contexts.default) {
            val items = injection.locals.foo.items.toMutableList()
            items.removeIf { item -> deleted.contains(item.id) }
            items.removeIf { item -> response.items.any { it.id == item.id } }
            items.addAll(response.items)
            injection.locals.foo.items = items
        }
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.items
        }
        _state.emit(State(loading = false, items = items))
    }

    private suspend fun onSyncMerge(result: Result<ItemsSyncMergeResponse>, deleted: List<UUID>) {
        if (result.isFailure) {
            logger.warning("sync merge: " + result.exceptionOrNull())
            _state.emit(State(loading = false, items = state.value?.items.orEmpty()))
            return
        }
        onSyncMerge(result.getOrThrow(), deleted = deleted)
    }

    private suspend fun onNeedUpdate(response: ItemsSyncResponse.NeedUpdate) {
        logger.debug("need update...")
        val download = mutableListOf<UUID>()
        val items = mutableListOf<Foo>()
        withContext(injection.contexts.default) {
            for (local in injection.locals.foo.metas) {
                val exists = response.metas.any { it.id == local.id }
                if (exists) continue
                val deleted = response.deleted.contains(local.id)
                if (deleted) continue
                val item = injection.locals.foo.items.firstOrNull { it.id == local.id } ?: TODO()
                items.add(item)
            }
            for (remote in response.metas) {
                val local = injection.locals.foo.metas.firstOrNull { it.id == remote.id }
                if (local == null) {
                    val deleted = injection.locals.foo.deleted.contains(remote.id)
                    if (deleted) continue
                    download.add(remote.id)
                } else if (remote.hash != local.hash) {
                    if (remote.updated > local.updated) {
                        download.add(remote.id)
                    } else {
                        val item = injection.locals.foo.items.firstOrNull { it.id == local.id } ?: TODO()
                        items.add(item)
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
                _state.emit(State(loading = false, items = state.value?.items.orEmpty()))
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
            _state.emit(State(loading = false, items = state.value?.items.orEmpty()))
            return
        }
        onResponse(result.getOrThrow())
    }

    fun syncItems() = launch {
        logger.debug("sync items...")
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val result = withContext(injection.contexts.default) {
            runCatching {
                val request = ItemsSyncRequest(
                    id = Foo.META_ID,
                    hash = injection.locals.foo.hash,
                )
                injection.remotes.itemsSync(request)
            }
        }
        onResponse(result)
    }
}
