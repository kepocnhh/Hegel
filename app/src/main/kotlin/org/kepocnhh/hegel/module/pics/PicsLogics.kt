package org.kepocnhh.hegel.module.pics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.FileRequest
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

    fun requestItems() = launch {
        logger.debug("request items...")
        _state.value = State(loading = true)
        _items.value = withContext(injection.contexts.default) {
            val pics = injection.storages.require<Pic>()
            Items(list = pics.items)
        }
        _state.value = State(loading = false)
    }

    fun addItem(title: String) = launch {
        _state.value = State(loading = true)
        _items.value = withContext(injection.contexts.default) {
            val pics = injection.storages.require<Pic>()
            pics.add(Pic(title = title, fd = null))
            Items(list = pics.items)
        }
        _state.value = State(loading = false)
    }

    fun deleteItem(id: UUID) = launch {
        _state.value = State(loading = true)
        _items.value = withContext(injection.contexts.default) {
            val pics = injection.storages.require<Pic>()
            pics.delete(id = id)
            Items(list = pics.items)
        }
        _state.value = State(loading = false)
    }

    fun setFile(id: UUID, bytes: ByteArray) = launch {
        logger.debug("set file: $id")
        _state.value = State(loading = true)
        _items.value = withContext(injection.contexts.default) {
            // todo partial
            val fd = FileDelegate(
                hash = injection.secrets.hash(bytes),
                size = bytes.size.toLong(),
            )
            val pics = injection.storages.require<Pic>()
            val payload = pics.require(id = id)
            injection.dirs.files.resolve(fd.name()).writeBytes(bytes)
            pics.update(id = id, value = payload.value.copy(fd = fd))
            Items(list = pics.items)
        }
        _state.value = State(loading = false)
    }

    private fun detachFile(pics: MutableStorage<Pic>, payload: Payload<Pic>) {
        val fd = payload.value.fd ?: return
        pics.update(id = payload.meta.id, value = payload.value.copy(fd = null))
        injection.dirs.files.resolve(fd.name()).delete()
    }

    private fun deleteFile(payload: Payload<Pic>) {
        val fd = payload.value.fd ?: return
        injection.dirs.files.resolve(fd.name()).delete()
    }

    fun detachFile(id: UUID) = launch {
        _state.value = State(loading = true)
        _items.value = withContext(injection.contexts.default) {
            val pics = injection.storages.require<Pic>()
            val payload = pics.require(id = id)
            detachFile(pics = pics, payload = payload)
            Items(list = pics.items)
        }
        _state.value = State(loading = false)
    }

    fun deleteFile(id: UUID) = launch {
        _state.value = State(loading = true)
        _items.value = withContext(injection.contexts.default) {
            val pics = injection.storages.require<Pic>()
            deleteFile(payload = pics.require(id = id))
            Items(list = pics.items)
        }
        _state.value = State(loading = false)
    }

    private fun downloadFile(fd: FileDelegate) {
        val address = injection.locals.address ?: error("No address!")
        val name = fd.name()
        val tmp = injection.dirs.cache.resolve(name)
        tmp.delete()
        var index: Long = 0
//        val count = 256
        val count = 2 shl 16
        while (true) {
            val request = FileRequest(
                fd = fd,
                index = index,
                count = kotlin.math.min(count, (fd.size - index).toInt()),
            )
            val bytes = injection.remotes.files(address).getBytes(request = request)
            tmp.appendBytes(bytes)
            index += bytes.size
            logger.debug("${request.index}] readed $name ${bytes.size}/${fd.size}")
            if (index > fd.size) TODO("Readed $index, but fd:size: ${fd.size}!")
            if (index == fd.size) {
                if (!fd.hash.contentEquals(injection.secrets.hash(tmp.readBytes()))) TODO("Hashes error!")
                break
            }
        }
        tmp.renameTo(injection.dirs.files.resolve(name))
    }

    fun downloadFile(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            runCatching {
                val pics = injection.storages.require<Pic>()
                val payload = pics.require(id = id)
                val fd = payload.value.fd ?: error("No file delegate!")
                downloadFile(fd = fd)
                Items(list = pics.items)
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
