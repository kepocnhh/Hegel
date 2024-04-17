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
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.FinalLoggers
import org.kepocnhh.hegel.provider.FinalRemotes
import org.kepocnhh.hegel.provider.Locals
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
        override var foo: List<Foo> = listOf(
            Foo(
                id = UUID.randomUUID(),
                text = "foo:1:text",
                created = System.currentTimeMillis().milliseconds - 2.hours,
            ),
            Foo(
                id = UUID.randomUUID(),
                text = "foo:2:text",
                created = System.currentTimeMillis().milliseconds - 1.hours,
            )
        )
    }

    override fun onCreate() {
        super.onCreate()
        _injection = Injection(
            contexts = Contexts(
                main = Dispatchers.Main,
                default = Dispatchers.Default,
            ),
            loggers = FinalLoggers,
            locals = MockLocals(),
            remotes = FinalRemotes(),
        )
    }

    companion object {
        private var _injection: Injection? = null

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
