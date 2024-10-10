package org.kepocnhh.hegel.module.baz

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Bar2Baz
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
            injection.storages.require<Baz>().items
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun deleteItem(id: UUID) = launch {
        logger.debug("delete $id")
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            val bazs = injection.storages.require<Baz>()
            val b2bs = injection.storages.require<Bar2Baz>()
            val payload = bazs.require(id = id)
            val ids = mutableMapOf(bazs.id to mutableSetOf(id))
            for (relation in b2bs.items) {
                if (relation.value.baz != payload.meta.id) continue
                logger.debug("delete relation with ${relation.value.bar}")
                ids.getOrPut(b2bs.id, ::mutableSetOf).add(relation.meta.id)
            }
            injection.storages.delete(ids = ids)
        }
        val list = withContext(injection.contexts.default) {
            injection.storages.require<Baz>().items
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
