package org.kepocnhh.hegel.module.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Keys
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.bytes.toHEX
import sp.kx.logics.Logics
import java.security.PrivateKey

internal class AuthLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    sealed interface Event {
        class OnAuth(val result: Result<Pair<Keys, PrivateKey>>) : Event
    }

    data class State(val loading: Boolean)

    private val logger = injection.loggers.create("[Auth]")

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _states = MutableStateFlow(State(loading = false))
    val states = _states.asStateFlow()

    fun auth(
        file: String,
        password: String,
        alias: String,
        pin: String,
    ) = launch {
        _states.value = State(loading = true)
        val result = withContext(injection.contexts.default) {
            runCatching {
                if (password.isBlank()) error("Password is blank!")
                if (pin.isBlank()) error("PIN is blank!")
                logger.debug("read \"$file\"...")
                val keyStore = injection.assets.getAsset(name = file).use {
                    logger.debug("load key store...")
                    injection.secrets.toKeyStore(it.readBytes(), password = password.toCharArray())
                }
                val privateKey = keyStore.getKey(alias, password.toCharArray()) ?: error("No \"$alias\"!")
                logger.debug("private:key: ${injection.secrets.hash(privateKey.encoded).toHEX()}")
                check(privateKey is PrivateKey)
                val certificate = keyStore.getCertificate(alias)
                logger.debug("certificate: ${injection.secrets.hash(certificate.encoded).toHEX()}")
                val publicKey = certificate.publicKey
                logger.debug("public:key: ${injection.secrets.hash(publicKey.encoded).toHEX()}")
                val secretKey = injection.secrets.getSecretKey(password = pin.toCharArray())
                logger.debug("secret:key: ${injection.secrets.hash(secretKey.encoded).toHEX()}")
                val keys = Keys(
                    publicKey = publicKey,
                    privateKeyEncrypted = injection.secrets.encrypt(secretKey, privateKey.encoded),
                )
                keys to privateKey
            }
        }
        _events.emit(Event.OnAuth(result))
        _states.value = State(loading = false)
    }
}
