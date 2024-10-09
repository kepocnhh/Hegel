package org.kepocnhh.hegel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import org.kepocnhh.hegel.module.app.Injection
import org.kepocnhh.hegel.provider.Contexts
import org.kepocnhh.hegel.provider.FinalAssets
import org.kepocnhh.hegel.provider.FinalLocals
import org.kepocnhh.hegel.provider.FinalLoggers
import org.kepocnhh.hegel.provider.FinalRemotes
import org.kepocnhh.hegel.provider.FinalSecrets
import org.kepocnhh.hegel.provider.FinalTLSEnvironment
import org.kepocnhh.hegel.provider.JsonSerializer
import org.kepocnhh.hegel.provider.MDHashFunction
import org.kepocnhh.hegel.provider.Serializer
import org.kepocnhh.hegel.provider.Sessions
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
    override fun onCreate() {
        super.onCreate()
        val hf = MDHashFunction("MD5")
        val serializer: Serializer = JsonSerializer(hf = hf)
        val env = object : SyncStreamsStorage.Environment {
            override fun now(): Duration {
                return System.currentTimeMillis().milliseconds
            }

            override fun randomUUID(): UUID {
                return UUID.randomUUID()
            }
        }
        val storages = SyncStreamsStorages.Builder()
            .add(UUID.fromString("10000000-89e9-45ee-945c-000000000001"), serializer.foo)
            .add(UUID.fromString("21000000-89e9-45ee-945c-000000000021"), serializer.bar)
            .add(UUID.fromString("22000000-89e9-45ee-945c-000000000022"), serializer.baz)
            .add(UUID.fromString("23000000-89e9-45ee-945c-000000000023"), serializer.bar2baz)
            .add(UUID.fromString("30000000-89e9-45ee-945c-000000000003"), serializer.pics)
            .build(
                hf = hf,
                env = env,
                dir = filesDir,
            )
        val secrets = FinalSecrets()
        val sessions = Sessions()
        val locals = FinalLocals(context = this, secrets = secrets)
        val tls = FinalTLSEnvironment(locals = locals, secrets = secrets, sessions = sessions)
        val loggers = FinalLoggers
        _injection = Injection(
            contexts = Contexts(
                main = Dispatchers.Main,
                default = Dispatchers.Default,
            ),
            loggers = loggers,
            locals = locals,
            storages = storages,
            remotes = FinalRemotes(serializer = serializer, tls = tls, loggers = loggers),
            serializer = serializer,
            sessions = sessions,
            assets = FinalAssets(context = this),
            secrets = secrets,
            tls = tls,
            filesDir = filesDir,
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
