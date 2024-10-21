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
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.absoluteValue
import kotlin.math.min

internal class FilesService : LifecycleService() {
    internal sealed interface Event {
        class OnDownload(val fd: FileDelegate) : Event
    }

    private val logger = App.injection.loggers.create("[Files|Service]")
    private val N_ID: Int = System.currentTimeMillis().plus(hashCode()).toInt().absoluteValue

    sealed interface State {
        data class Downloading(val fd: FileDelegate?) : State
        data object Stopped : State
    }

    private fun onState(state: State) {
        logger.debug("state: $state")
        when (state) {
            is State.Downloading -> {
                val fd = state.fd
                if (fd == null) {
                    perform()
                } else {
                    lifecycleScope.launch {
                        withContext(App.injection.contexts.default) {
                            runCatching {
                                download(fd = fd)
                            }
                        }.onFailure { error: Throwable ->
                            logger.warning("download ${fd.name()} error: $error")
                            _states.value = State.Stopped
                        }
                    }
                }
            }
            State.Stopped -> {
                _queue.value = ConcurrentLinkedQueue()
                stopForeground(STOP_FOREGROUND_REMOVE)
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(N_ID)
            }
        }
    }

    private fun buildNotification(
        context: Context,
        text: CharSequence,
        silent: Boolean,
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
            .setSilent(silent)
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
        _queue.value = ConcurrentLinkedQueue()
        lifecycleScope.launch {
            states.flowWithLifecycle(lifecycle).collect(::onState)
        }
    }

    private fun NotificationManager.startForeground(
        context: Context,
        text: CharSequence,
        silent: Boolean,
        progress: Int,
    ) {
        val notification = buildNotification(
            context = context,
            text = text,
            silent = silent,
            progress = progress,
        )
        notify(N_ID, notification)
        startForeground(N_ID, notification)
    }

    private suspend fun download(fd: FileDelegate) {
        logger.debug("start download: ${fd.name()}")
        val context: Context = this
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.startForeground(context = context, text = "0%", silent = false, progress = 0)
        val address = App.injection.locals.address ?: error("No address!")
        val name = fd.name()
        val tmp = App.injection.dirs.cache.resolve(name)
        tmp.delete()
        var index: Long = 0
//        val count = 2 shl 10
        val count = 2 shl 16
        while (true) {
            val request = FileRequest(
                fd = fd,
                index = index,
                count = min(count, (fd.size - index).toInt()),
            )
            val bytes = App.injection.remotes.files(address).getBytes(request = request)
            tmp.appendBytes(bytes)
            index += bytes.size
            logger.debug("${request.index}] readed $name ${bytes.size}/${fd.size}")
            if (index > fd.size) TODO("Readed $index, but fd:size: ${fd.size}!")
            val progress = (index.toDouble() / fd.size * 100).toInt()
            nm.startForeground(
                context = context,
                text = "${fd.name()}\n$index/${fd.size}",
                silent = true,
                progress = progress,
            )
            if (index == fd.size) {
                val hash = App.injection.secrets.hash(tmp.readBytes())
                if (!fd.hash.contentEquals(hash)) TODO("Hashes error!")
                break
            }
            if (states.value == State.Stopped) return
        }
        tmp.renameTo(App.injection.dirs.files.resolve(name))
        _events.emit(Event.OnDownload(fd = fd))
        logger.debug("finish download: ${fd.name()}")
        _states.value = State.Downloading(fd = null)
    }

    private fun perform() {
        when (val state = states.value) {
            is State.Downloading -> {
                if (state.fd != null) return
                val fds = ConcurrentLinkedQueue(queue.value)
                val fd = fds.poll()
                _queue.value = fds
                if (fd == null) {
                    logger.debug("perform:no file delegate")
                    _states.value = State.Stopped
                } else {
                    logger.debug("perform:download: $fd")
                    _states.value = State.Downloading(fd = fd)
                }
            }
            State.Stopped -> {
                val fds = ConcurrentLinkedQueue(queue.value)
                val fd = fds.poll()
                _queue.value = fds
                if (fd != null) {
                    logger.debug("perform:stopped -> download: $fd")
                    _states.value = State.Downloading(fd = fd)
                }
            }
        }
    }

    private fun onStartCommand(intent: Intent) {
        logger.debug("command:${intent.action}")
        when (intent.action) {
            "download" -> {
                val fd = intent.getParcelableExtra<FileDelegateParcelable>("fd")
                    ?.delegate
                    ?: error("No file delegate!")
                when (states.value) {
                    is State.Downloading -> {
                        val fds = queue.value
                        if (fds.contains(fd)) return // todo
                        logger.debug("download: $fd")
                        _queue.value = fds + fd
                        perform()
                    }
                    State.Stopped -> {
                        logger.debug("download: $fd")
                        _states.value = State.Downloading(fd = fd)
                    }
                }
            }
            "stop" -> {
                _states.value = State.Stopped
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

        private val _states = MutableStateFlow<State>(State.Stopped)
        val states = _states.asStateFlow()
        private val _events = MutableSharedFlow<Event>()
        val events = _events.asSharedFlow()
        private val _queue = MutableStateFlow<Collection<FileDelegate>>(ConcurrentLinkedQueue())
        val queue = _queue.asStateFlow()

        fun download(context: Context, fd: FileDelegate) {
            val intent = Intent(context, FilesService::class.java)
            intent.action = "download"
            intent.putExtra("fd", FileDelegateParcelable(fd))
            context.startService(intent)
        }
    }
}
