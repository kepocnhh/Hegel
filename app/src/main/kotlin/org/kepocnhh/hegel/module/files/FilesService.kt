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
import sp.kx.bytes.loader.BytesLoader
import kotlin.math.absoluteValue

internal class FilesService : LifecycleService() {
    private val logger = App.injection.loggers.create("[Files|Service]")
    private val N_ID: Int = System.currentTimeMillis().plus(hashCode()).toInt().absoluteValue

    /* todo
    private fun onState(queue: BytesLoader<String>.BytesQueue) {
        logger.debug("queue: $queue")
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (queue.states.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            nm.cancel(N_ID)
        } else {
            val key = queue.current ?: TODO("No current!")
            val current = queue.states[key] ?: TODO("No state by $key!")
            nm.startForeground(
                context = this,
                text = "$key\n${current.loaded}/${current.size}",
                progress = current.progress(),
            )
        }
    }
    */

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
//            states.flowWithLifecycle(lifecycle).collect(::onState) // todo
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

/*
    private suspend fun postDownload(fd: FileDelegate, tmp: File) {
        val queue = HashMap(states.value.queue)
        val name = fd.name()
        val loaded = tmp.length()
        if (loaded > fd.size) TODO("Read $loaded, but fd:size: ${fd.size}!")
        if (loaded == fd.size) {
            val hash = App.injection.secrets.hash(tmp.readBytes())
            if (!fd.hash.contentEquals(hash)) TODO("Hashes error!")
            if (App.injection.dirs.files.resolve(name).exists()) TODO("file: $name exists!")
            tmp.renameTo(App.injection.dirs.files.resolve(name))
            queue.remove(fd)
            logger.debug("finish download: $name")
            _events.emit(Event.OnDownload(fd = fd))
        } else {
            queue[fd] = loaded
        }
        _states.value = State(queue = queue, current = null)
    }
*/

/*
    private fun download(fd: FileDelegate, loaded: Long): File {
        logger.debug("start download: ${fd.name()}")
        val address = App.injection.locals.address ?: error("No address!")
        val name = fd.name()
        val tmp = App.injection.dirs.cache.resolve(name)
        val index: Long
        if (loaded == 0L) {
            tmp.delete()
            index = 0
        } else {
            if (tmp.length() != loaded) TODO("Length: ${tmp.length()}, but loaded: $loaded!")
            index = loaded
        }
        val count = 2 shl 10
//        val count = 2 shl 16
        val request = FileRequest(
            name = name,
            size = fd.size,
            index = index,
            count = kotlin.math.min(count, (fd.size - index).toInt()),
        )
        val bytes = App.injection.remotes.files(address).getBytes(request = request)
        tmp.appendBytes(bytes)
        logger.debug("${request.index}] read $name ${bytes.size}/${fd.size}")
        return tmp
    }
*/

/*
    private fun perform() {
        val state = states.value
        if (state.current != null) return
        val (fd, loaded) = state.queue.entries.firstOrNull() ?: return
        _states.value = state.copy(current = fd)
        logger.debug("perform:download: $fd")
        lifecycleScope.launch {
            withContext(App.injection.contexts.default) {
                runCatching {
                    val tmp = download(fd = fd, loaded = loaded)
                    if (states.value.queue.containsKey(fd)) postDownload(fd = fd, tmp = tmp)
                }
            }.fold(
                onSuccess = {
                    perform()
                },
                onFailure = { error: Throwable ->
                    logger.warning("download ${fd.name()} error: $error")
                    _states.value = State(queue = emptyMap(), current = null)
                },
            )
        }
    }
*/

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
            count = 1 shl 16,
        )
        private val NC_ID = "f6d353de-3d4d-4abf-8f6b-053c5ccdec09"

        val events = loader.events
//        val states = loader.states // todo

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
    }
}
