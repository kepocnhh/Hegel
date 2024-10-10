package org.kepocnhh.hegel.module.bar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Bar2Baz
import org.kepocnhh.hegel.entity.Baz
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import sp.kx.storages.Payload
import sp.kx.storages.require
import java.util.UUID

internal class BarLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    data class BarView(
        val parent: Payload<Bar>,
        val children: List<Payload<Baz>>,
    )

    data class Items(
        val list: List<BarView>,
    )

    private val logger = injection.loggers.create("[Bar]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _items = MutableStateFlow<Items?>(null)
    val items = _items.asStateFlow()

    private fun getItems(): List<BarView> {
        val bars = injection.storages.require<Bar>()
        val bazs = injection.storages.require<Baz>()
        val b2bs = injection.storages.require<Bar2Baz>()
        return bars.items.map { parent ->
            val relations = b2bs.filter { it.value.bar == parent.meta.id }
            BarView(
                parent = parent,
                children = bazs.filter { child ->
                    relations.any { it.value.baz == child.meta.id }
                },
            )
        }
    }

    fun requestItems() = launch {
        _state.value = State(loading = true)
        val list = withContext(injection.contexts.default) {
            getItems()
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun deleteItem(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            val bars = injection.storages.require<Bar>()
            val bazs = injection.storages.require<Baz>()
            val b2bs = injection.storages.require<Bar2Baz>()
            val parent = bars.require(id = id)
            val ids = mutableMapOf(bars.id to mutableSetOf(id))
            for (relation in b2bs.items) {
                if (relation.value.bar != parent.meta.id) continue
                logger.debug("delete relation with ${relation.value.baz}")
                ids.getOrPut(b2bs.id, ::mutableSetOf).add(relation.meta.id)
                ids.getOrPut(bazs.id, ::mutableSetOf).add(relation.value.baz)
            }
            injection.storages.delete(ids = ids)
        }
        val list = withContext(injection.contexts.default) {
            getItems()
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun addItem(count: Int) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            val bar = Bar(count = count)
            val payload = injection.storages.require<Bar>().add(bar)
            for (i in 0 until (count % 3 + 1)) {
                val baz = Baz(title = "$i of ${payload.meta.id.toString().substring(0 until 8)}")
                val child = injection.storages.require<Baz>().add(baz)
                val relation = Bar2Baz(bar = payload.meta.id, baz = child.meta.id)
                injection.storages.require<Bar2Baz>().add(relation)
            }
        }
        val list = withContext(injection.contexts.default) {
            getItems()
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun updateItem(id: UUID, count: Int) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            val payload = injection.storages.require<Bar>().require(id = id)
            injection.storages.require<Bar>().update(id = id, payload.value.copy(count = count))
        }
        val list = withContext(injection.contexts.default) {
            getItems()
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
