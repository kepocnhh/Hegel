package org.kepocnhh.hegel.module.enter

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.bytes.toHEX
import sp.kx.logics.Logics
import java.security.PrivateKey

internal class EnterLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Event {
        class OnEnter(val result: Result<PrivateKey>) : Event
    }

    data class State(val loading: Boolean)

    private val logger = injection.loggers.create("[Enter]")

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _states = MutableStateFlow(State(loading = false))
    val states = _states.asStateFlow()

    fun enter(pin: String) = launch {
        _states.value = State(loading = true)
        val result = withContext(injection.contexts.default) {
            runCatching {
                if (pin.isBlank()) error("PIN is blank!")
                val secretKey = injection.secrets.getSecretKey(password = pin.toCharArray())
                logger.debug("secret:key: ${injection.secrets.hash(secretKey.encoded).toHEX()}")
                val keys = injection.locals.keys ?: TODO()
                val decrypted = injection.secrets.decrypt(secretKey, keys.privateKeyEncrypted)
                logger.debug("private:key: ${injection.secrets.hash(decrypted).toHEX()}")
                injection.secrets.toPrivateKey(decrypted)
            }
        }
        _events.emit(Event.OnEnter(result))
        _states.value = State(loading = false)
    }
}
