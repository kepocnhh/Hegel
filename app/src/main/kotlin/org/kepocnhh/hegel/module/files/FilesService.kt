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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.BuildConfig
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.FileDelegateParcelable
import org.kepocnhh.hegel.entity.FileRequest
import java.io.File
import kotlin.math.absoluteValue

internal class FilesService : LifecycleService() {
    internal sealed interface Event {
        class OnDownload(val fd: FileDelegate) : Event
    }

    private val logger = App.injection.loggers.create("[Files|Service]")
    private val N_ID: Int = System.currentTimeMillis().plus(hashCode()).toInt().absoluteValue

    data class State(
        val queue: Map<FileDelegate, Long>,
        val current: FileDelegate?,
    )

    private fun onState(state: State) {
        logger.debug("state: $state")
        val fd = state.current
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (fd == null) {
            if (state.queue.isEmpty()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            nm.cancel(N_ID)
        } else {
            val loaded = state.queue[fd] ?: TODO("No fd: $fd")
            val progress = (loaded.toDouble() / fd.size * 100).toInt()
            nm.startForeground(
                context = this,
                text = "${fd.name()}\n$loaded/${fd.size}",
                progress = progress,
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
            fd = fd,
            index = index,
            count = kotlin.math.min(count, (fd.size - index).toInt()),
        )
        val bytes = App.injection.remotes.files(address).getBytes(request = request)
        tmp.appendBytes(bytes)
        logger.debug("${request.index}] read $name ${bytes.size}/${fd.size}")
        return tmp
    }

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

    private fun onStartCommand(intent: Intent) {
        logger.debug("command:${intent.action}")
        when (intent.action) {
            "download" -> {
                val fd = intent.getParcelableExtra<FileDelegateParcelable>("fd")
                    ?.delegate
                    ?: error("No file delegate!")
                val state = states.value
                if (state.queue.containsKey(fd) || state.current?.equals(fd) == true) return // todo
                if (App.injection.dirs.files.resolve(fd.name()).exists()) {
                    logger.debug("file: ${fd.name()} exists")
                    return
                }
                logger.debug("download: $fd")
                val queue = HashMap(state.queue)
                queue[fd] = 0
                _states.value = state.copy(queue = queue)
                perform()
            }
            "stop" -> {
                _states.value = State(queue = emptyMap(), current = null)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (intent != null) onStartCommand(intent)
        return result
    }

    companion object {
        private val NC_ID = "f6d353de-3d4d-4abf-8f6b-053c5ccdec09"

        private val _states = MutableStateFlow(State(queue = emptyMap(), current = null))
        val states = _states.asStateFlow()
        private val _events = MutableSharedFlow<Event>()
        val events = _events.asSharedFlow()

        fun download(context: Context, fd: FileDelegate) {
            val intent = Intent(context, FilesService::class.java)
            intent.action = "download"
            intent.putExtra("fd", FileDelegateParcelable(fd))
            context.startService(intent)
        }
    }
}
