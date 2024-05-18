package org.kepocnhh.hegel.util.http

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket

internal abstract class HttpService(
    private val _state: MutableStateFlow<State>,
) : Service() {
    sealed interface State {
        data object Stopped : State
        data object Stopping : State
        data class Started(val address: String) : State
        data object Starting : State
    }

    enum class Action {
        StartForeground,
        StopForeground,
        StartServer,
        StopServer,
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var oldState: State = _state.value
    private var serverSocket: ServerSocket? = null

    private fun onStartServer() {
        if (_state.value != State.Stopped) TODO()
        _state.value = State.Starting
    }

    private fun onStopServer() {
        if (_state.value !is State.Started) TODO()
        _state.value = State.Stopping
    }

    private fun onStartCommand(intent: Intent) {
        val intentAction = intent.action ?: error("No intent action!")
        if (intentAction.isBlank()) error("Intent action is blank!")
        val action = Action.entries.firstOrNull { it.name == intentAction } ?: error("No action!")
        when (action) {
            Action.StartForeground -> TODO()
            Action.StopForeground -> TODO()
            Action.StartServer -> onStartServer()
            Action.StopServer -> onStopServer()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) error("No intent!")
        onStartCommand(intent)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun onStopping() {
        if (_state.value != State.Stopping) TODO()
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    checkNotNull(serverSocket).close()
                } finally {
                    serverSocket = null
                }
            }
        }
    }

    protected abstract fun onSocketAccept(request: HttpRequest): HttpResponse

    private fun onStarting(serverSocket: ServerSocket) {
        val address = try {
            getInetAddress().hostAddress ?: error("No host address!")
        } catch (e: Throwable) {
            println("[HttpService] get address error: $e") // todo
            runCatching { serverSocket.close() }.onFailure {
                println("[HttpService] socket($serverSocket) close error: $it") // todo
            }
            _state.value = State.Stopped
            return
        }
        if (this.serverSocket != null) TODO()
        this.serverSocket = serverSocket
        _state.value = State.Started("$address:${serverSocket.localPort}")
        while (_state.value is State.Started) {
            try {
                serverSocket.accept().use { socket ->
                    val request = HttpRequest.read(socket.getInputStream().bufferedReader())
                    val response = onSocketAccept(request)
                    HttpResponse.write(response, socket.getOutputStream())
                }
            } catch (e: Throwable) {
                if (_state.value is State.Stopping) break
                TODO("HttpService:onStarting:$e")
            }
        }
        _state.value = State.Stopped
    }

    private fun onStarting() {
        if (_state.value != State.Starting) TODO()
        scope.launch {
            withContext(Dispatchers.IO) {
//                val portNumber = 0 // todo
                val portNumber = 40631
                onStarting(ServerSocket(portNumber))
            }
        }
    }

    private fun onState(newState: State) {
        val oldState = oldState
        this.oldState = newState
        when (oldState) {
            is State.Started -> onStopping()
            State.Starting -> when (newState) {
                is State.Started -> {
                    // todo
                }
                State.Starting -> TODO()
                State.Stopped -> {
                    // todo
                }
                State.Stopping -> TODO()
            }
            State.Stopped -> onStarting()
            State.Stopping -> when (newState) {
                is State.Started -> TODO()
                State.Starting -> TODO()
                State.Stopped -> {
                    // todo
                }
                State.Stopping -> TODO()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        _state.onEach(::onState).launchIn(scope)
    }

    companion object {
        inline fun <reified T : HttpService> startService(context: Context, action: Action) {
            val intent = Intent(context, T::class.java)
            intent.action = action.name
            context.startService(intent)
        }

        private fun getInetAddress(): InetAddress {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (!interfaces.hasMoreElements()) error("No interfaces!")
            return interfaces
                .asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress }
                ?: error("No addresses!")
        }
    }
}
