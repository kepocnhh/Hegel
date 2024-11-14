package org.kepocnhh.hegel.module.files

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.BuildConfig
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.FileDelegateParcelable
import org.kepocnhh.hegel.provider.FinalBytesRequester
import org.kepocnhh.hegel.provider.FinalBytesWrapper
import sp.kx.bytes.loader.BytesLoaded
import sp.kx.bytes.loader.BytesLoader
import kotlin.math.absoluteValue

internal class FilesService : LifecycleService() {
    private val logger = App.injection.loggers.create("[Files|Service]")
    private val N_ID: Int = System.currentTimeMillis().plus(hashCode()).toInt().absoluteValue

    private fun onState(state: BytesLoader.State?) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (state == null) {
            logger.debug("no state")
            stopForeground(STOP_FOREGROUND_REMOVE)
            nm.cancel(N_ID)
        } else {
            logger.debug("queue: ${state.queue}\ncurrent: ${state.current}")
            val bl = state.queue[state.current] ?: TODO("No state by ${state.current}!")
            nm.startForeground(
                context = this,
                text = "${state.current}\n${bl.loaded}/${bl.size}",
                progress = progress(bl = bl),
            )
        }
    }

    private fun buildNotification(
        context: Context,
        text: CharSequence,
        progress: Int,
    ): Notification {
        val intent = Intent(this, FilesService::class.java)
        intent.action = "stop"
        val deleteIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(context, NC_ID)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentText(text)
            .setAutoCancel(false)
            .setOngoing(true)
            .setDeleteIntent(deleteIntent)
            .setSilent(true)
            .setProgress(100, progress, false)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(NC_ID) == null) {
            val name = "${BuildConfig.APPLICATION_ID}:${this::class.java.simpleName}"
            val channel = NotificationChannel(NC_ID, name, NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        lifecycleScope.launch {
            states.flowWithLifecycle(lifecycle).collect(::onState)
        }
    }

    private fun NotificationManager.startForeground(
        context: Context,
        text: CharSequence,
        progress: Int,
    ) {
        val notification = buildNotification(
            context = context,
            text = text,
            progress = progress,
        )
        notify(N_ID, notification)
        startForeground(N_ID, notification)
    }

    private fun onStartCommand(intent: Intent) {
        logger.debug("command:${intent.action}")
        when (intent.action) {
            "download" -> {
                val fd = intent.getParcelableExtra<FileDelegateParcelable>("fd")
                    ?.delegate
                    ?: error("No file delegate!")
                lifecycleScope.launch {
                    withContext(App.injection.contexts.default) {
                        loader.add(
                            uri = fd.uri,
                            size = fd.size,
                            hash = fd.hash,
                        )
                    }
                }
            }
            "stop" -> {
                loader.stop()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (intent != null) onStartCommand(intent)
        return result
    }

    companion object {
        private val loader = BytesLoader(
            factory = FinalBytesWrapper.Factory(
                dirs = App.injection.dirs,
                secrets = App.injection.secrets,
            ),
            requester = FinalBytesRequester(
                locals = App.injection.locals,
                remotes = App.injection.remotes,
            ),
//            count = 1 shl 10, //     1_024
//            count = 1 shl 16, //    65_536
            count = 1 shl 18, //   262_144
//            count = 1 shl 20, // 1_048_576
        )
        private val NC_ID = "f6d353de-3d4d-4abf-8f6b-053c5ccdec09"

        val events = loader.events
        val states = loader.states

        // todo downloadAll
        fun download(context: Context, fd: FileDelegate) {
            val intent = Intent(context, FilesService::class.java)
            intent.action = "download"
            intent.putExtra("fd", FileDelegateParcelable(fd))
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FilesService::class.java)
            intent.action = "stop"
            context.startService(intent)
        }

        fun progress(bl: BytesLoaded): Int {
            return (bl.loaded.toDouble() / bl.size * 100).toInt()
        }
    }
}
