package org.kepocnhh.hegel.module.bar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import sp.kx.storages.Described
import sp.kx.storages.MutableStorage
import java.util.UUID

internal class BarLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    data class Items(
        val list: List<Described<Bar>>,
    )

    private val logger = injection.loggers.create("[Bar]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _items = MutableStateFlow<Items?>(null)
    val items = _items.asStateFlow()

    private fun getStorage(): MutableStorage<Bar> {
        return injection.storages.require()
    }

    fun requestItems() = launch {
        _state.value = State(loading = true)
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.info.created }
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
            getStorage().items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun addItem(count: Int) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            getStorage().add(Bar(count = count))
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun updateItem(id: UUID, count: Int) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            getStorage().update(id = id, Bar(count = count))
        }
        val list = withContext(injection.contexts.default) {
            getStorage().items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
