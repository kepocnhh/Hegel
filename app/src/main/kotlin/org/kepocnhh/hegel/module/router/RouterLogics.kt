package org.kepocnhh.hegel.module.router

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Keys
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import java.security.PrivateKey
import java.security.PublicKey

internal class RouterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface State {
        data object NoKeys : State
        class Keys(val publicKey: PublicKey, val authorized: Boolean) : State
    }

    private val _states = MutableStateFlow<State?>(null)
    val states = _states.asStateFlow()

    fun requestState() = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) {
            _states.value = State.NoKeys
        } else {
            val authorized = withContext(injection.contexts.default) {
                injection.sessions.privateKey != null
            }
            _states.value = State.Keys(publicKey = keys.publicKey, authorized = authorized)
        }
    }

    fun lock() = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) TODO()
        withContext(injection.contexts.default) {
            injection.sessions.privateKey = null // todo secureConnection
        }
        _states.value = State.Keys(publicKey = keys.publicKey, authorized = false)
    }

    fun exit() = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) TODO()
        withContext(injection.contexts.default) {
            injection.locals.keys = null
        }
        _states.value = State.NoKeys
    }

    fun enter(privateKey: PrivateKey) = launch {
        val keys = withContext(injection.contexts.default) {
            injection.locals.keys
        }
        if (keys == null) TODO()
        withContext(injection.contexts.default) {
            injection.sessions.privateKey = privateKey
        }
        _states.value = State.Keys(publicKey = keys.publicKey, authorized = true)
    }

    fun auth(keys: Keys, privateKey: PrivateKey) = launch {
        withContext(injection.contexts.default) {
            if (injection.locals.keys != null) TODO()
            injection.locals.keys = keys
        }
        withContext(injection.contexts.default) {
            injection.sessions.privateKey = privateKey
        }
        _states.value = State.Keys(publicKey = keys.publicKey, authorized = true)
    }
}
