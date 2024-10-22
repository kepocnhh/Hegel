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
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.absoluteValue

internal class FilesService : LifecycleService() {
    internal sealed interface Event {
        class OnDownload(val fd: FileDelegate) : Event
    }

    private val logger = App.injection.loggers.create("[Files|Service]")
    private val N_ID: Int = System.currentTimeMillis().plus(hashCode()).toInt().absoluteValue

    data class StateOld(
        val queue: Collection<FileDelegate>,
        val current: Current?
    ) {
        data class Current(
            val fd: FileDelegate,
            val downloaded: Long,
        )
    }

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
            val downloaded = state.queue[fd] ?: TODO("No fd: $fd")
            val progress = (downloaded.toDouble() / fd.size * 100).toInt()
            nm.startForeground(
                context = this,
                text = "${fd.name()}\n$downloaded/${fd.size}",
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

    private suspend fun postDownload(fd: FileDelegate, tmp: File) {
        val queue = HashMap(states.value.queue)
        val name = fd.name()
        val downloaded = tmp.length()
        if (downloaded > fd.size) TODO("Readed $downloaded, but fd:size: ${fd.size}!")
        if (downloaded == fd.size) {
            val hash = App.injection.secrets.hash(tmp.readBytes())
            if (!fd.hash.contentEquals(hash)) TODO("Hashes error!")
            if (App.injection.dirs.files.resolve(name).exists()) TODO("file: $name exists!")
            tmp.renameTo(App.injection.dirs.files.resolve(name))
            queue.remove(fd)
            logger.debug("finish download: $name")
            _events.emit(Event.OnDownload(fd = fd))
        } else {
            queue[fd] = downloaded
        }
        _states.value = State(queue = queue, current = null)
    }

    private fun download(fd: FileDelegate, downloaded: Long): File {
        logger.debug("start download: ${fd.name()}")
        val address = App.injection.locals.address ?: error("No address!")
        val name = fd.name()
        val tmp = App.injection.dirs.cache.resolve(name)
        val index: Long
        if (downloaded == 0L) {
            tmp.delete()
            index = 0
        } else {
            if (tmp.length() != downloaded) TODO("Length: ${tmp.length()}, but downloaded: $downloaded!")
            index = downloaded
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
        logger.debug("${request.index}] readed $name ${bytes.size}/${fd.size}")
        return tmp
    }

/*
    private fun downloadMock(fd: FileDelegate) {
        logger.debug("mock:start:download: ${fd.name()}")
        val name = fd.name()
        var index: Long = 0
        val count = 2 shl 10
//        val count = 2 shl 16
        while (true) {
            if (stopped.get()) {
                logger.debug("mock:stop:externally: ${fd.name()}")
                throw StopException()
            }
            val request = FileRequest(
                fd = fd,
                index = index,
                count = kotlin.math.min(count, (fd.size - index).toInt()),
            )
            Thread.sleep(100)
            index += request.count
            logger.debug("${request.index}] readed $name ${request.count}/${fd.size}")
            if (index > fd.size) TODO("Readed $index, but fd:size: ${fd.size}!")
            if (index == fd.size) break
            _states.value = states.value.copy(current = State.Current(fd = fd, downloaded = index))
        }
    }
*/

/*
    private suspend fun downloadOld(fd: FileDelegate) {
        logger.debug("start download: ${fd.name()}")
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
                throw StopException()
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
            if (index == fd.size) {
                val hash = App.injection.secrets.hash(tmp.readBytes())
                if (!fd.hash.contentEquals(hash)) TODO("Hashes error!")
                break
            }
            _states.value = states.value.copy(current = State.Current(fd = fd, downloaded = index))
        }
        if (App.injection.dirs.files.resolve(name).exists()) TODO("file: $name exists!")
        tmp.renameTo(App.injection.dirs.files.resolve(name))
    }
*/

    private fun perform() {
        val state = states.value
        if (state.current != null) return
        val (fd, downloaded) = state.queue.entries.firstOrNull() ?: return
        _states.value = state.copy(current = fd)
        logger.debug("perform:download: $fd")
        lifecycleScope.launch {
            withContext(App.injection.contexts.default) {
                runCatching {
                    val tmp = download(fd = fd, downloaded = downloaded)
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

/*
    private fun performOld() {
        val state = states.value
        if (state.current != null) return
        val fds = ConcurrentLinkedQueue(state.queue)
        val fd = fds.poll()
        if (fd == null) {
            logger.debug("perform:no file delegate")
        } else {
            _states.value = State(queue = fds, current = State.Current(fd = fd, downloaded = 0))
            logger.debug("perform:download: $fd")
            lifecycleScope.launch {
                withContext(App.injection.contexts.default) {
                    runCatching {
                        download(fd = fd)
                    }
                }.fold(
                    onSuccess = {
                        logger.debug("finish download: ${fd.name()}")
                        _events.emit(Event.OnDownload(fd = fd))
                        _states.value = states.value.copy(current = null)
                        perform()
                    },
                    onFailure = { error: Throwable ->
                        logger.warning("download ${fd.name()} error: $error")
                        _states.value = State(queue = emptyMap(), current = null)
                    },
                )
            }
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
