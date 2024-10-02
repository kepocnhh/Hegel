package org.kepocnhh.hegel.module.pics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.Pic
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import sp.kx.storages.MutableStorage
import sp.kx.storages.Payload
import sp.kx.storages.require
import java.util.UUID

internal class PicsLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    data class Items(
        val list: List<Payload<Pic>>,
    )

    private val logger = injection.loggers.create("[Pics]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _items = MutableStateFlow<Items?>(null)
    val items = _items.asStateFlow()

    private fun getStorage(): MutableStorage<Pic> {
        return injection.storages.require()
    }

    fun requestItems() = launch {
        logger.debug("request items...")
        _state.value = State(loading = true)
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun addItem(title: String) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            getStorage().add(
                Pic(
                    title = title,
                    fileId = null,
                ),
            )
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun deleteItem(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            getStorage().delete(id = id)
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun setFile(id: UUID, bytes: ByteArray) = launch {
        logger.debug("set file: $id")
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            val filesDir = injection.filesDir
            val fileDelegate = FileDelegate(
                hash = injection.secrets.hash(bytes),
                size = bytes.size,
            )
            val payload = injection.storages.require<FileDelegate>().add(value = fileDelegate)
            filesDir.resolve(payload.meta.id.toString()).writeBytes(bytes)
            val pic = getStorage().items.firstOrNull { it.meta.id == id }?.value ?: TODO()
            getStorage().update(id = id, value = pic.copy(fileId = payload.meta.id))
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
