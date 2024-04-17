package org.kepocnhh.hegel.module.foo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class FooLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
        val items: List<Foo>,
    )

    private val logger = injection.loggers.create("[Foo]")
    private val _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    fun requestItems() = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo
        }
        _state.emit(State(loading = false, items = items))
    }

    fun deleteItem(id: UUID) = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.toMutableList().also { list ->
                list.removeIf { it.id == id }
                injection.locals.foo = list
            }
        }
        _state.emit(State(loading = false, items = items))
    }

    fun addItem(text: String) = launch {
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val items = withContext(injection.contexts.default) {
            injection.locals.foo.toMutableList().also { list ->
                list += Foo(
                    id = UUID.randomUUID(),
                    created = System.currentTimeMillis().milliseconds,
                    text = text,
                )
                injection.locals.foo = list
            }
        }
        _state.emit(State(loading = false, items = items))
    }

    private suspend fun onResponse(response: ItemsSyncResponse) {
        when (response) {
            ItemsSyncResponse.NotModified -> {
                _state.emit(State(loading = false, items = state.value?.items.orEmpty()))
                return
            }
        }
    }

    private suspend fun onResponse(result: Result<ItemsSyncResponse>) {
        if (result.isFailure) {
            logger.warning("sync items: " + result.exceptionOrNull())
            _state.emit(State(loading = false, items = state.value?.items.orEmpty()))
            return
        }
        onResponse(result.getOrThrow())
    }

    fun syncItems() = launch {
        logger.debug("sync items...")
        _state.emit(State(loading = true, items = state.value?.items.orEmpty()))
        val result = withContext(injection.contexts.default) {
            runCatching {
                injection.remotes.itemsSync()
            }
        }
        onResponse(result)
    }
}
