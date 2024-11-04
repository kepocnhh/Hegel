package org.kepocnhh.hegel.provider

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

abstract class BytesLoader<K : Any> {
    sealed interface Event<K : Any> {
        class OnLoad<K : Any>(val key: K) : Event<K>
        class OnError<K : Any>(val key: K, val error: Throwable) : Event<K>
    }

    inner class BytesState(
        val size: Long,
        val loaded: Long,
    ) {
        fun progress(): Int {
            return (loaded.toDouble() / size * 100).toInt()
        }

        override fun toString(): String {
            return "BS($size/$loaded)"
        }
    }

    inner class BytesQueue(
        val states: Map<K, BytesState>,
        val current: K?,
    ) {
        override fun toString(): String {
            return "BQ($states/$current)"
        }
    }

    private val _events = MutableSharedFlow<Event<K>>()
    val events = _events.asSharedFlow()

    private val _states = MutableStateFlow<BytesQueue>(BytesQueue(states = emptyMap(), current = null))
    val states = _states.asStateFlow()

    //    private val count = 2 shl 10
//    private val count = 2 shl 12
    private val count = 2 shl 14
//    private val count = 2 shl 16

    private val queue: MutableMap<K, BytesRequest> = HashMap()
    private val loading = AtomicBoolean(false)

    private inner class BytesRequest(
        val size: Long,
        val hash: ByteArray,
    ) {
        var loaded: Long = 0
    }

    protected interface BytesWrapper {
        fun clear()
        fun size(): Long
        fun append(bytes: ByteArray)
        fun hash(): ByteArray
        fun commit()
    }

    protected abstract fun getWrapper(key: K): BytesWrapper

    protected abstract fun getBytes(
        key: K,
        size: Long,
        index: Long,
        count: Int,
    ): ByteArray

    private fun load(
        key: K,
        size: Long,
        loaded: Long,
        wrapper: BytesWrapper,
    ) {
        if (loaded == 0L) {
            wrapper.clear()
        } else {
            val length = wrapper.size()
            if (length != loaded) TODO("Length: $length, but loaded: $loaded!")
        }
        val bytes = getBytes(
            key = key,
            size = size,
            index = loaded,
            count = kotlin.math.min(count, (size - loaded).toInt()),
        )
        wrapper.append(bytes)
    }

    private fun getLoaded(
        size: Long,
        hash: ByteArray,
        wrapper: BytesWrapper,
    ): Long? {
        val loaded = wrapper.size()
        if (loaded > size) TODO("Read $loaded, but size: $size!")
        if (loaded == size) {
            if (!hash.contentEquals(wrapper.hash())) TODO("Hashes error!")
            wrapper.commit()
            return null
        }
        return loaded
    }

    private suspend fun perform() {
        if (!loading.compareAndSet(false, true)) return
        while (true) {
            val (key, request) = queue.entries.firstOrNull() ?: break
            _states.value = BytesQueue(
                states = queue.mapValues { (_, value) ->
                    BytesState(size = value.size, loaded = value.loaded)
                },
                current = key,
            )
            perform(key = key, request = request)
        }
        _states.value = BytesQueue(states = emptyMap(), current = null)
        loading.set(false)
    }

    private suspend fun perform(key: K, request: BytesRequest) {
        val wrapper = getWrapper(key = key)
        val loaded = try {
            load(
                key = key,
                size = request.size,
                loaded = request.loaded,
                wrapper = wrapper,
            )
            getLoaded(
                size = request.size,
                hash = request.hash,
                wrapper = wrapper,
            )
        } catch (error: Throwable) {
            queue.clear() // todo
            _events.emit(Event.OnError(key = key, error = error))
            return
        }
        if (loaded == null) {
            queue.remove(key)
            _events.emit(Event.OnLoad(key = key))
        } else {
            request.loaded = loaded
        }
    }

    suspend fun load(
        key: K,
        size: Long,
        hash: ByteArray,
    ) {
        synchronized(this) {
            if (!queue.containsKey(key)) {
                val request = BytesRequest(
                    size = size,
                    hash = hash,
                )
                queue[key] = request
            }
        }
        perform()
    }

    fun stop() {
        queue.clear()
    }
}
