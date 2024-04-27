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
import org.kepocnhh.hegel.entity.Bar
import org.kepocnhh.hegel.entity.Described
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Info
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.entity.map
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

    private class MockStorage<T : Any>(override val id: UUID) : Storage<T> {
        override val items = mutableListOf<Described<T>>()
        override var hash: String = "0"
        override val deleted = mutableSetOf<UUID>()

        override fun delete(id: UUID) {
            val index = items.indexOfFirst { it.id == id }
            if (index < 0) return
            items.removeAt(index)
            deleted += id
            hash = items.hash()
        }

        override fun add(item: T) {
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

        override fun update(id: UUID, item: T) {
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

        override fun merge(items: List<Described<T>>, deleted: Set<UUID>) {
            this.items.removeIf { item -> deleted.contains(item.id) }
            this.items.removeIf { item -> items.any { it.id == item.id } }
            this.items.addAll(items)
            this.deleted += deleted
            hash = this.items.hash()
        }

        private fun Iterable<Described<out Any>>.hash(): String {
            return sortedBy {
                it.info.created
            }.joinToString(separator = "") {
                it.info.hash
            }.hashCode().toString()
        }
    }

    private class MockLocals : Locals {
        override val bar: Storage<Bar> = MockStorage(id = Bar.STORAGE_ID)
        override val foo: Storage<Foo> = MockStorage(id = Foo.STORAGE_ID)

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
