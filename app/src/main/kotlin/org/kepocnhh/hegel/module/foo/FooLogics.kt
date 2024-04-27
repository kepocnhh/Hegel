package org.kepocnhh.hegel.module.foo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import java.util.UUID

internal class FooLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    data class Items(
        val list: List<Described<Foo>>,
    )

    private val logger = injection.loggers.create("[Foo]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _items = MutableStateFlow<Items?>(null)
    val items = _items.asStateFlow()

    fun requestItems() = launch {
        _state.value = State(loading = true)
        val list = withContext(injection.contexts.default) {
            injection.locals.foo.items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun deleteItem(id: UUID) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            injection.locals.foo.delete(id = id)
        }
        val list = withContext(injection.contexts.default) {
            injection.locals.foo.items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun addItem(text: String) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            injection.locals.foo.add(Foo(text = text))
        }
        val list = withContext(injection.contexts.default) {
            injection.locals.foo.items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }

    fun updateItem(id: UUID, text: String) = launch {
        _state.value = State(loading = true)
        withContext(injection.contexts.default) {
            injection.locals.foo.update(id = id, Foo(text = text))
        }
        val list = withContext(injection.contexts.default) {
            injection.locals.foo.items.sortedBy { it.info.created }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
