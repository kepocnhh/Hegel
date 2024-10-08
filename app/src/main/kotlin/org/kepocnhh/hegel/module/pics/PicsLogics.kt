package org.kepocnhh.hegel.module.pics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.Pic
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.util.toHEX
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
                    fd = null,
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
            val fd = FileDelegate(
                hash = injection.secrets.hash(bytes),
                size = bytes.size,
            )
//            val payload = injection.storages.require<FileDelegate>().add(value = fileDelegate)
            val payload = getStorage().items.firstOrNull { it.meta.id == id } ?: TODO()
            val name = "${payload.meta.id}-${fd.hash.copyOf(16).toHEX()}"
            injection.filesDir.resolve(name).writeBytes(bytes)
            getStorage().update(id = id, value = payload.value.copy(fd = fd))
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    private fun deleteFile(payload: Payload<Pic>) {
        val fd = payload.value.fd ?: return
        getStorage().update(id = payload.meta.id, value = payload.value.copy(fd = null))
        val name = "${payload.meta.id}-${fd.hash.copyOf(16).toHEX()}"
        injection.filesDir.resolve(name).delete()
    }

    fun deleteFile(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            val payload = getStorage().items.firstOrNull { it.meta.id == id } ?: TODO()
            deleteFile(payload = payload)
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun downloadFile(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            runCatching {
                val address = injection.locals.address ?: error("No address!")
                val bytes = injection.remotes.files(address).getFile(id = id)
                logger.debug("get ${bytes.size} bytes...")
                val fd = FileDelegate(
                    hash = injection.secrets.hash(bytes),
                    size = bytes.size,
                )
                val payload = getStorage().items.firstOrNull { it.meta.id == id } ?: TODO("No pic by id: $id")
                if (fd != payload.value.fd) TODO("e: $fd, a: ${payload.value.fd}")
                val name = "${payload.meta.id}-${fd.hash.copyOf(16).toHEX()}"
                injection.filesDir.resolve(name).writeBytes(bytes)
            }.mapCatching {
                val list = withContext(injection.contexts.default) {
                    getStorage().items.sortedBy { it.meta.created }
                }
                Items(list = list)
            }
        }.fold(
            onSuccess = {
                _items.value = it
            },
            onFailure = { error ->
                logger.warning("get file by id: $id error: $error")
            },
        )
        _state.value = State(loading = false)
    }
}
