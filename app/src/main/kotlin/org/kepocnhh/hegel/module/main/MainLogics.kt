package org.kepocnhh.hegel.module.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics

internal class MainLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    class State(val publicKeyHash: ByteArray)

    private val _states = MutableStateFlow<State?>(null)
    val states = _states.asStateFlow()

    fun requestState() = launch {
        val publicKeyHash = withContext(injection.contexts.default) {
            val keys = injection.locals.keys ?: error("No keys!")
            injection.secrets.hash(keys.publicKey.encoded)
        }
        _states.value = State(publicKeyHash = publicKeyHash)
    }
}
