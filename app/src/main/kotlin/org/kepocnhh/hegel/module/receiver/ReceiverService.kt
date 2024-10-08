package org.kepocnhh.hegel.module.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.BuildConfig
import sp.kx.http.HttpReceiver
import kotlin.math.absoluteValue

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
        if (intentAction == "onDelete") {
            // todo
            logger.debug("on delete...")
            coroutineScope.launch {
                onState(receiver.states.value)
            }
            return
        }
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

    private fun buildNotification(
        context: Context,
        text: CharSequence,
    ): Notification {
        val intent = Intent(this, ReceiverService::class.java)
        intent.action = "onDelete"
        val deleteIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, NC_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentText(text)
            .setAutoCancel(false)
            .setOngoing(true)
            .setDeleteIntent(deleteIntent)
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
