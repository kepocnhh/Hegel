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
import org.kepocnhh.hegel.entity.Foo
import org.kepocnhh.hegel.entity.Meta
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
import kotlin.time.Duration.Companion.hours
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
            override var metas: List<Meta> = emptyList()
            override var meta: Meta = Meta(
                id = Foo.META_ID,
                updated = System.currentTimeMillis().milliseconds,
                hash = metas.joinToString(separator = "") { it.hash }.hashCode().toString(), // todo
            )
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
                        Meta(
                            id = item.id,
                            updated = System.currentTimeMillis().milliseconds,
                            hash = item.hashCode().toString(), // todo
                        )
                    }
                    this.metas = metas
                    meta = meta.copy(
                        updated = System.currentTimeMillis().milliseconds,
                        hash = metas.joinToString(separator = "") { it.hash }.hashCode().toString(), // todo
                    )
                    field = value
                }
            override var deleted: List<UUID> = emptyList()
        }

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
