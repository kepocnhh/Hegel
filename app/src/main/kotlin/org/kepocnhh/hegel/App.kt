package org.kepocnhh.hegel

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Dispatchers
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.FileStreamerProvider
import org.kepocnhh.hegel.provider.FinalLocals
import org.kepocnhh.hegel.provider.FinalLoggers
import org.kepocnhh.hegel.provider.FinalRemotes
import org.kepocnhh.hegel.provider.JsonSerializer
import org.kepocnhh.hegel.provider.MDHashFunction
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.util.compose.toPaddings
import sp.kx.logics.Logics
import sp.kx.logics.LogicsFactory
import sp.kx.logics.LogicsProvider
import sp.kx.logics.contains
import sp.kx.logics.get
import sp.kx.logics.remove
import sp.kx.storages.SyncStreamsStorage
import sp.kx.storages.SyncStreamsStorages
import java.util.UUID
import kotlin.time.Duration
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
            content: @Composable () -> Unit,
        ) {
            CompositionLocalProvider(
                LocalInsets provides LocalView.current.rootWindowInsets.toPaddings(),
                content = content,
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        val serializer: Serializer = JsonSerializer()
        val hf = MDHashFunction("MD5")
        val env = object : SyncStreamsStorage.Environment {
            override fun now(): Duration {
                return System.currentTimeMillis().milliseconds
            }

            override fun randomUUID(): UUID {
                return UUID.randomUUID()
            }
        }
        val storages = SyncStreamsStorages.Builder()
            .add(UUID.fromString("84e44670-d301-471b-a7ac-dfd8b1e55554"), serializer.foo)
            .add(UUID.fromString("6c7a0b49-89e9-45ee-945c-0faad06a3df7"), serializer.bar)
            .build(
                hf = hf,
                env = env,
                getStreamerProvider = { ids: Set<UUID> ->
                    FileStreamerProvider(dir = filesDir, ids = ids)
                },
            )
        _injection = Injection(
            contexts = Contexts(
                main = Dispatchers.Main,
                default = Dispatchers.Default,
            ),
            loggers = FinalLoggers,
            locals = FinalLocals(context = this),
            storages = storages,
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
