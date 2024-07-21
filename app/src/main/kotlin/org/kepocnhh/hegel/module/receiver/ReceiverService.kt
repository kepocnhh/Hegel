package org.kepocnhh.hegel.module.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.BuildConfig
import org.kepocnhh.hegel.entity.ItemsSyncMergeRequest
import org.kepocnhh.hegel.entity.ItemsSyncMergeResponse
import org.kepocnhh.hegel.entity.ItemsSyncResponse
import org.kepocnhh.hegel.entity.Session
import org.kepocnhh.hegel.util.http.HttpRequest
import org.kepocnhh.hegel.util.http.HttpResponse
import org.kepocnhh.hegel.util.http.HttpService
import org.kepocnhh.hegel.util.toHEX
import sp.kx.http.HttpReceiver
import sp.kx.storages.CommitInfo
import sp.kx.storages.Described
import sp.kx.storages.SyncInfo
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class ReceiverService : Service() {
    enum class Action {
        Start,
        Stop,
    }

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)
    private val logger = App.injection.loggers.create("[Receiver|Service]")

    private fun onStartServer() {
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                val port = 40631 // todo
                receiver.start(port = port)
            }
        }
    }

    private fun onStopServer() {
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                receiver.stop()
            }
        }
    }

    private fun onStartCommand(intent: Intent) {
        val intentAction = intent.action ?: error("No intent action!")
        if (intentAction.isBlank()) error("Intent action is blank!")
        val action = Action.entries.firstOrNull { it.name == intentAction } ?: error("No action!")
        when (action) {
            Action.Start -> onStartServer()
            Action.Stop -> onStopServer()
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

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(NC_ID) == null) {
            val channel = NotificationChannel(NC_ID, "${BuildConfig.APPLICATION_ID}:notifications", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                receiver.states.collect { state ->
                    onState(state = state)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        receiver.stop() // todo
    }

    private fun buildNotification(
        context: Context,
        text: CharSequence,
    ): Notification {
        return NotificationCompat.Builder(context, NC_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentText(text)
            .build()
    }

    private val N_ID: Int = System.currentTimeMillis().toInt().absoluteValue

    private suspend fun onState(state: HttpReceiver.State) = withContext(Dispatchers.Main) {
        logger.debug("on state: $state")
        val context: Context = this@ReceiverService
        when (state) {
            is HttpReceiver.State.Started -> {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                val notification = buildNotification(context, if (state.stopping) "stopping" else "started")
                nm.notify(N_ID, notification)
                startForeground(N_ID, notification)
            }
            is HttpReceiver.State.Stopped -> {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                if (state.starting) {
                    val notification = buildNotification(context, "starting")
                    nm.notify(N_ID, notification)
                    startForeground(N_ID, notification)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    nm.cancel(N_ID)
                }
            }
        }
    }

    companion object {
        private val receiver: HttpReceiver = HttpReceiver(ReceiverRouting(App.injection))
        val states = receiver.states
        private val NC_ID = "058bb727-9be2-401c-a2eb-3f141749a7cc"

        fun startService(context: Context, action: Action) {
            val intent = Intent(context, ReceiverService::class.java)
            intent.action = action.name
            context.startService(intent)
        }
    }
}

@Deprecated(message = "org.kepocnhh.hegel.module.receiver.ReceiverService")
internal class ReceiverServiceOld : HttpService(_state) {
    private val logger = App.injection.loggers.create("[Receiver]")
    private val routing = mapOf(
        "/v1/items/sync" to mapOf(
            "POST" to ::onPostItemsSync,
        ),
        "/v1/items/merge" to mapOf(
            "POST" to ::onPostItemsMerge,
        ),
    )

    private fun onItemsMerge(request: ItemsSyncMergeRequest): HttpResponse {
        val oldSession = App.injection.locals.session
        if (oldSession == null) {
            return HttpResponse(
                code = 500,
                message = "TODO", // todo
            )
        }
        if (oldSession.id != request.sessionId) {
            return HttpResponse(
                code = 500,
                message = "TODO", // todo
            )
        }
        if (oldSession.expires < System.currentTimeMillis().milliseconds) {
            App.injection.locals.session = null
            return HttpResponse(
                code = 500,
                message = "TODO", // todo
            )
        }
        val commits = App.injection.storages.merge(infos = request.merges)
        val response = ItemsSyncMergeResponse(commits = commits)
        App.injection.locals.session = null
        val body = App.injection.serializer.remote.mergeResponse.encode(response)
        return HttpResponse(
            code = 200,
            message = "OK",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to body.size.toString(),
            ),
            body = body,
        )
    }

    private fun onPostItemsMerge(request: HttpRequest): HttpResponse {
        logger.debug("on post items merge...")
        val bytes = request.body ?: TODO()
        return onItemsMerge(App.injection.serializer.remote.syncMerge.decode(bytes))
    }

    private fun onItemsSync(hashes: Map<UUID, ByteArray>): HttpResponse {
        val oldSession = App.injection.locals.session
        if (oldSession != null) {
            if (oldSession.expires > System.currentTimeMillis().milliseconds) {
                return HttpResponse(
                    code = 500,
                    message = "TODO", // todo
                )
            } else {
                App.injection.locals.session = null
            }
        }
        val syncs = App.injection.storages.getSyncInfo(hashes)
        if (syncs.isEmpty()) {
            logger.debug("not modified")
            return HttpResponse(
                code = 304,
                message = "Not Modified",
            )
        }
        val session = Session(
            id = UUID.randomUUID(),
            expires = System.currentTimeMillis().milliseconds + 1.minutes,
        )
        App.injection.locals.session = session
        logger.debug("syncs: ${syncs.mapValues { (_, si) -> si.infos.mapValues { (_, ii) -> ii.hash.toHEX() } }}") // todo
        val response = ItemsSyncResponse(
            sessionId = session.id,
            syncs = syncs,
        )
        val body = App.injection.serializer.remote.syncResponse.encode(response)
        return HttpResponse(
            code = 200,
            message = "OK",
            headers = mapOf(
                "Content-Type" to "application/json",
                "Content-Length" to body.size.toString(),
            ),
            body = body,
        )
    }

    private fun onPostItemsSync(request: HttpRequest): HttpResponse {
        logger.debug("on post items sync...")
        val bytes = request.body ?: TODO()
        val syncRequest = App.injection.serializer.remote.syncRequest.decode(bytes)
        return onItemsSync(hashes = syncRequest.hashes)
    }

    override fun onSocketAccept(request: HttpRequest): HttpResponse {
        val route = routing[request.query] ?: return HttpResponse(
            code = 404,
            message = "No Found",
        )
        val transform = route[request.method] ?: return HttpResponse(
            code = 405,
            message = "Method Not Allowed",
        )
        return transform(request)
    }

    companion object {
        private val _state = MutableStateFlow<State>(State.Stopped)
        val state = _state.asStateFlow()
    }
}
