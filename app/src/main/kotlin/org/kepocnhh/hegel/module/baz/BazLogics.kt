package org.kepocnhh.hegel.module.baz

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Baz
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import sp.kx.storages.Payload
import sp.kx.storages.require
import java.util.UUID

internal class BazLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    data class Items(
        val list: List<Payload<Baz>>,
    )

    private val logger = injection.loggers.create("[Baz]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _items = MutableStateFlow<Items?>(null)
    val items = _items.asStateFlow()

    fun requestItems() = launch {
        _state.value = State(loading = true)
        val list = withContext(injection.contexts.default) {
            injection.storages.require<Baz>().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun deleteItem(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            TODO("BazLogics:deleteItem($id)")
        }
        val list = withContext(injection.contexts.default) {
            injection.storages.require<Baz>().items.sortedBy { it.meta.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
