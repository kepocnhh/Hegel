package org.kepocnhh.hegel.module.transmitter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncRequest
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.MergeInfo
import org.kepocnhh.hegel.entity.StorageInfo
import org.kepocnhh.hegel.entity.map
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.provider.Storage
import org.kepocnhh.hegel.provider.Transformer
import sp.kx.logics.Logics
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

    private suspend fun onSyncMerge(response: ItemsSyncMergeResponse, deleted: Map<UUID, Set<UUID>>) {
        logger.debug("sync merge...")
        withContext(injection.contexts.default) {
            for ((storageId, encoded) in response.storages) {
                when (storageId) {
                    Foo.STORAGE_ID -> {
                        val items = encoded.map {
                            it.map(injection.serializer.fooItem::decode)
                        }
                        injection.locals.foo.merge(items, deleted[storageId].orEmpty())
                    }
                    Bar.STORAGE_ID -> {
                        val items = encoded.map {
                            it.map(injection.serializer.barItem::decode)
                        }
                        injection.locals.bar.merge(items, deleted[storageId].orEmpty())
                    }
                    else -> TODO()
                }
            }
        }
        // todo
        val hashes = setOf(
            injection.locals.foo,
            injection.locals.bar,
        ).associate { storage ->
            storage.id to storage.hash
        }
        logger.debug("hashes: $hashes")
        // todo
        _state.value = State(loading = false)
        _broadcast.emit(Broadcast.OnSync(Result.success(Unit)))
    }

    private suspend fun onSyncMerge(result: Result<ItemsSyncMergeResponse>, deleted: Map<UUID, Set<UUID>>) {
        if (result.isFailure) {
            val error = result.exceptionOrNull() ?: TODO()
            logger.warning("sync merge: $error")
            _state.value = State(loading = false)
            _broadcast.emit(Broadcast.OnSync(Result.failure(error)))
            return
        }
        onSyncMerge(result.getOrThrow(), deleted = deleted)
    }

    private fun <T : Any> getMergeInfo(
        storage: Storage<T>,
        storageInfo: StorageInfo,
        transformer: Transformer<T>,
    ): MergeInfo {
        val download = mutableSetOf<UUID>()
        val items = mutableListOf<Described<ByteArray>>()
        for (described in storage.items) {
            if (storageInfo.meta.containsKey(described.id)) continue
            if (storageInfo.deleted.contains(described.id)) continue
            items.add(described.map(transformer::encode))
        }
        for ((id, info) in storageInfo.meta) {
            val described = storage.items.firstOrNull { it.id == id }
            if (described == null) {
                if (storage.deleted.contains(id)) continue
                download.add(id)
            } else if (info.hash != described.info.hash) {
                if (info.updated > described.info.updated) {
                    download.add(id)
                } else {
                    items.add(described.map(transformer::encode))
                }
            }
        }
        logger.debug("download[${storage.id}]: $download")
        return MergeInfo(
            download = download,
            items = items,
            deleted = storage.deleted,
        )
    }

    private suspend fun onNeedUpdate(response: ItemsSyncResponse.NeedUpdate) {
        logger.debug("need update...")
        logger.debug("storages: ${response.storages.map { (storageId, info) -> storageId to info.meta.map { (id, i) -> id to i.hash } }}")
        val storages = mutableMapOf<UUID, MergeInfo>()
        withContext(injection.contexts.default) {
            for ((storageId, storageInfo) in response.storages) {
                val mergeInfo = when (storageId) {
                    Foo.STORAGE_ID -> {
                        getMergeInfo(
                            storage = injection.locals.foo,
                            storageInfo = storageInfo,
                            transformer = injection.serializer.fooItem,
                        )
                    }
                    Bar.STORAGE_ID -> {
                        getMergeInfo(
                            storage = injection.locals.bar,
                            storageInfo = storageInfo,
                            transformer = injection.serializer.barItem,
                        )
                    }
                    else -> TODO()
                }
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
        onSyncMerge(result, deleted = response.storages.mapValues { (_, value) -> value.deleted })
    }

    private suspend fun onResponse(response: ItemsSyncResponse) {
        when (response) {
            ItemsSyncResponse.NotModified -> {
                logger.debug("not modified")
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
                        injection.locals.foo,
                        injection.locals.bar,
                    ).associate { it.id to it.hash },
                )
                logger.debug("hashes: ${request.hashes}")
                injection.remotes.itemsSync(request)
            }
        }
        onResponse(result)
    }
}
