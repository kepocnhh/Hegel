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
import kotlinx.coroutines.delay
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.absoluteValue

internal class FilesService : LifecycleService() {
    internal sealed interface Event {
        class OnDownload(val fd: FileDelegate) : Event
    }

    private val logger = App.injection.loggers.create("[Files|Service]")
    private val N_ID: Int = System.currentTimeMillis().plus(hashCode()).toInt().absoluteValue
    private val stopped = AtomicBoolean(true)

    data class State(
        val queue: Collection<FileDelegate>,
        val current: Current?
    ) {
        data class Current(
            val fd: FileDelegate,
            val downloaded: Long,
        )
    }

    private fun onState(state: State) {
        logger.debug("state: $state")
        val current = state.current
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (current == null) {
            if (state.queue.isEmpty()) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
            nm.cancel(N_ID)
        } else {
            val progress = (current.downloaded.toDouble() / current.fd.size * 100).toInt()
            nm.startForeground(
                context = this,
                text = "${current.fd.name()}\n${current.downloaded}/${current.fd.size}",
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

    private class StopException : Exception()

    private suspend fun download(fd: FileDelegate) {
        logger.debug("mock:start:download: ${fd.name()}")
        _states.value = states.value.copy(current = State.Current(fd = fd, downloaded = 0))
        var index: Long = 0
        val count = 2 shl 10
//        val count = 2 shl 16
        while (true) {
            if (stopped.get()) {
                logger.debug("mock:stop:externally: ${fd.name()}")
                throw StopException()
            }
            delay(100)
//            delay(250)
            index = kotlin.math.min(fd.size, index + count)
            _states.value = states.value.copy(current = State.Current(fd = fd, downloaded = index))
            if (index == fd.size) {
                break
            }
        }
        _events.emit(Event.OnDownload(fd = fd))
        logger.debug("mock:finish:download: ${fd.name()}")
        _states.value = states.value.copy(current = null)
    }

    private suspend fun downloadOld(fd: FileDelegate) {
        logger.debug("start download: ${fd.name()}")
        _states.value = states.value.copy(current = State.Current(fd = fd, downloaded = 0))
        val address = App.injection.locals.address ?: error("No address!")
        val name = fd.name()
        val tmp = App.injection.dirs.cache.resolve(name)
        tmp.delete()
        var index: Long = 0
        val count = 2 shl 10
//        val count = 2 shl 16
        while (true) {
            if (stopped.get()) {
                logger.debug("stop externally: ${fd.name()}")
                _states.value = states.value.copy(queue = emptyList(), current = null)
                return
            }
            val request = FileRequest(
                fd = fd,
                index = index,
                count = kotlin.math.min(count, (fd.size - index).toInt()),
            )
            val bytes = App.injection.remotes.files(address).getBytes(request = request)
            tmp.appendBytes(bytes)
            index += bytes.size
            logger.debug("${request.index}] readed $name ${bytes.size}/${fd.size}")
            if (index > fd.size) TODO("Readed $index, but fd:size: ${fd.size}!")
            _states.value = states.value.copy(current = State.Current(fd = fd, downloaded = index))
            if (index == fd.size) {
                val hash = App.injection.secrets.hash(tmp.readBytes())
                if (!fd.hash.contentEquals(hash)) TODO("Hashes error!")
                break
            }
        }
        tmp.renameTo(App.injection.dirs.files.resolve(name))
        _events.emit(Event.OnDownload(fd = fd))
        logger.debug("finish download: ${fd.name()}")
        _states.value = states.value.copy(current = null)
    }

    private fun perform() {
        val state = states.value
        val current = state.current
        if (current != null) return
        val fds = ConcurrentLinkedQueue(state.queue)
        val fd = fds.poll()
        if (fd == null) {
            logger.debug("perform:no file delegate")
        } else {
            _states.value = state.copy(queue = fds)
            logger.debug("perform:download: $fd")
            lifecycleScope.launch {
                withContext(App.injection.contexts.default) {
                    runCatching {
                        download(fd = fd)
                    }
                }.onFailure { error: Throwable ->
                    logger.warning("download ${fd.name()} error: $error")
                    _states.value = State(queue = emptyList(), current = null)
                }.onSuccess {
                    perform()
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
                val state = states.value
                if (state.queue.contains(fd)) return // todo
                logger.debug("download: $fd")
                _states.value = state.copy(queue = state.queue + fd)
                stopped.set(false)
                perform()
            }
            "stop" -> {
                stopped.set(true)
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

        private val _states = MutableStateFlow(State(queue = emptyList(), current = null))
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
