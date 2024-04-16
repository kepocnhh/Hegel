package org.kepocnhh.hegel.module.foo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics

internal class FooLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
        val items: List<Foo>,
    )
    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    fun requestItems() = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo
        }
        _state.emit(State(loading = false, items = items))
    }
}
