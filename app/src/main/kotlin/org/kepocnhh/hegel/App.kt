package org.kepocnhh.hegel

import android.app.Application
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Dispatchers
import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Info
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.FinalLoggers
import org.kepocnhh.hegel.provider.FinalRemotes
import org.kepocnhh.hegel.provider.JsonSerializer
import org.kepocnhh.hegel.provider.Locals
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.provider.Storage
import org.kepocnhh.hegel.util.compose.LocalOnBackPressedDispatcher
import org.kepocnhh.hegel.util.compose.toPaddings
import sp.kx.logics.Logics
import sp.kx.logics.LogicsFactory
import sp.kx.logics.LogicsProvider
import sp.kx.logics.contains
import sp.kx.logics.get
import sp.kx.logics.remove
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class App : Application() {
    object Theme {
        private val LocalInsets = staticCompositionLocalOf<PaddingValues> { error("No insets!") }

        val insets: PaddingValues
            @Composable
            @ReadOnlyComposable
            get() = LocalInsets.current

        @Composable
        fun Composition(
            onBackPressedDispatcher: OnBackPressedDispatcher,
            content: @Composable () -> Unit,
        ) {
            CompositionLocalProvider(
                LocalOnBackPressedDispatcher provides onBackPressedDispatcher,
                LocalInsets provides LocalView.current.rootWindowInsets.toPaddings(),
                content = content,
            )
        }
    }

    private class MockLocals : Locals {
        override var foo: Storage<Foo> = object : Storage<Foo> {
            override val id: UUID = Foo.STORAGE_ID
            override val items = mutableListOf<Described<Foo>>()
            override var hash: String = items.hash()
            override val deleted = mutableListOf<UUID>()

            override fun delete(id: UUID) {
                val index = items.indexOfFirst { it.id == id }
                if (index < 0) return
                items.removeAt(index)
                deleted += id
                hash = items.hash()
            }

            override fun add(item: Foo) {
                val created = System.currentTimeMillis().milliseconds
                items += Described(
                    id = UUID.randomUUID(), // todo
                    info = Info(
                        created = created,
                        updated = created,
                        hash = item.hashCode().toString(), // todo
                    ),
                    item = item,
                )
                hash = items.hash()
            }

            override fun update(id: UUID, item: Foo) {
                val index = items.indexOfFirst { it.id == id }
                if (index < 0) TODO()
                val oldItem = items[index]
                items.removeAt(index)
                items += oldItem.copy(
                    info = oldItem.info.copy(
                        updated = System.currentTimeMillis().milliseconds,
                        hash = item.hashCode().toString(), // todo
                    ),
                    item = item,
                )
                hash = items.hash()
            }

            override fun merge(items: List<Described<Foo>>, deleted: List<UUID>) {
                this.items.removeIf { item -> deleted.contains(item.id) }
                this.items.removeIf { item -> items.any { it.id == item.id } }
                this.items.addAll(items)
                hash = items.hash()
            }

            private fun Iterable<Described<Foo>>.hash(): String {
                return joinToString(separator = "") { it.info.hash }.hashCode().toString()
            }
        }

        /*
        override var foo: Storage<Foo> = object : Storage<Foo> {
            override val id: UUID = Foo.META_ID
            override var metas: List<Meta> = emptyList()
            override var hash: String = metas.joinToString(separator = "") { it.hash }.hashCode().toString() // todo
            override var items: List<Foo> = emptyList()
                set(value) {
                    val deleted = deleted.toMutableSet()
                    for (old in field) {
                        if (value.none { it.id == old.id }) {
                            deleted += old.id
                        }
                    }
                    this.deleted = deleted.toList()
                    val metas = value.map { item ->
                        val hash = item.hashCode().toString() // todo
                        metas.firstOrNull { it.id == item.id }
                            ?.takeIf { it.hash == hash }
                            ?: Meta(
                                id = item.id,
                                updated = System.currentTimeMillis().milliseconds,
                                hash = hash,
                            )
                    }
//                    val equals = metas.sortedBy { it.id }.map { it.hash } == this.metas.sortedBy { it.id }.map { it.hash } // todo
                    this.metas = metas
                    hash = metas.joinToString(separator = "") { it.hash }.hashCode().toString() // todo
                    field = value
                }
            override var deleted: List<UUID> = emptyList()
        }
        */

        override var session: Session? = null
    }

    override fun onCreate() {
        super.onCreate()
        val serializer: Serializer = JsonSerializer()
        _injection = Injection(
            contexts = Contexts(
                main = Dispatchers.Main,
                default = Dispatchers.Default,
            ),
            loggers = FinalLoggers,
            locals = MockLocals(),
            remotes = FinalRemotes(serializer = serializer),
            serializer = serializer,
        )
    }

    companion object {
        private var _injection: Injection? = null
        val injection: Injection get() = checkNotNull(_injection)

        private val _logicsProvider = LogicsProvider(
            factory = object : LogicsFactory {
                override fun <T : Logics> create(type: Class<T>): T {
                    val injection = checkNotNull(_injection) { "No injection!" }
                    return type
                        .getConstructor(Injection::class.java)
                        .newInstance(injection)
                }
            },
        )
        @Composable
        inline fun <reified T : Logics> logics(label: String = T::class.java.name): T {
            val (contains, logic) = synchronized(App::class.java) {
                remember { _logicsProvider.contains<T>(label = label) } to _logicsProvider.get<T>(label = label)
            }
            DisposableEffect(Unit) {
                onDispose {
                    synchronized(App::class.java) {
                        if (!contains) {
                            _logicsProvider.remove<T>(label = label)
                        }
                    }
                }
            }
            return logic
        }
    }
}
