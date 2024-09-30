package org.kepocnhh.hegel.module.pics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.entity.Pic
import org.kepocnhh.hegel.module.app.Injection
import sp.kx.logics.Logics
import sp.kx.storages.ItemInfo
import sp.kx.storages.Metadata
import sp.kx.storages.Payload
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class PicsLogics(
    private val injection: Injection,
) : Logics(injection.contexts.main) {
    data class State(
        val loading: Boolean,
    )

    data class Items(
        val list: List<Payload<Pic>>,
    )

    private val logger = injection.loggers.create("[Pics]")
    private val _state = MutableStateFlow(State(loading = false))
    val state = _state.asStateFlow()
    private val _items = MutableStateFlow<Items?>(null)
    val items = _items.asStateFlow()

    fun requestItems() = launch {
        logger.debug("request items...")
        _state.value = State(loading = true)
        val list = withContext(injection.contexts.default) {
            val now = System.currentTimeMillis().milliseconds
            (0 until 24).map { index ->
                val created = now - 1.hours + index.minutes
                Payload(
                    meta = Metadata(
                        id = UUID(0, index.toLong()),
                        created = created,
                        info = ItemInfo(
                            updated = created,
                            hash = byteArrayOf(index.toByte()),
                        )
                    ),
                    value = Pic(title = "pic #$index"),
                )
            }
        }
        _items.value = Items(list = list)
        _state.value = State(loading = false)
    }
}
