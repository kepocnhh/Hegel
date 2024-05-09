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
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.EncryptedFileStorage
import org.kepocnhh.hegel.provider.FinalLoggers
import org.kepocnhh.hegel.provider.FinalRemotes
import org.kepocnhh.hegel.provider.JsonSerializer
import org.kepocnhh.hegel.provider.Locals
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.util.compose.LocalOnBackPressedDispatcher
import org.kepocnhh.hegel.util.compose.toPaddings
import sp.kx.logics.Logics
import sp.kx.logics.LogicsFactory
import sp.kx.logics.LogicsProvider
import sp.kx.logics.contains
import sp.kx.logics.get
import sp.kx.logics.remove
import sp.kx.storages.SyncStorages
import java.util.UUID

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
            locals = MockLocals(), // todo
            storages = SyncStorages.Builder()
                .add(
                    EncryptedFileStorage(
                        id = UUID.fromString("84e44670-d301-471b-a7ac-dfd8b1e55554"),
                        context = this,
                        transformer = serializer.foo,
                    ),
                )
                .add(
                    EncryptedFileStorage(
                        id = UUID.fromString("6c7a0b49-89e9-45ee-945c-0faad06a3df7"),
                        context = this,
                        transformer = serializer.bar,
                    ),
                )
                .build(),
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
