package org.kepocnhh.hegel.module.files

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.BuildConfig
import org.kepocnhh.hegel.entity.FileDelegate
import org.kepocnhh.hegel.entity.FileDelegateParcelable
import org.kepocnhh.hegel.entity.FileRequest
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min

internal class FilesService : LifecycleService() {
    internal sealed interface Event {
        class OnDownload(val fd: FileDelegate) : Event
    }

    private val logger = App.injection.loggers.create("[Files|Service]")

    sealed interface State {
        class Downloading(val fd: FileDelegate?) : State
        data object Stopped : State
    }

    private fun onState(state: State) {
        when (state) {
            is State.Downloading -> {
                val fd = state.fd
                lifecycleScope.launch {
                    if (fd == null) {
                        perform()
                    } else {
                        download(fd = fd)
                    }
                }
            }
            State.Stopped -> {
                // noop
            }
        }
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

    private val fds: Queue<FileDelegate> = ConcurrentLinkedQueue()

    private suspend fun download(fd: FileDelegate) {
        val address = App.injection.locals.address ?: error("No address!")
        val name = fd.name()
        val tmp = App.injection.dirs.cache.resolve(name)
        tmp.delete()
        var index: Long = 0
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
            if (index == fd.size) {
                val hash = App.injection.secrets.hash(tmp.readBytes())
                if (!fd.hash.contentEquals(hash)) TODO("Hashes error!")
                break
            }
            if (states.value == State.Stopped) return
        }
        tmp.renameTo(App.injection.dirs.files.resolve(name))
        _events.emit(Event.OnDownload(fd = fd))
        _states.value = State.Downloading(fd = null)
    }

    private fun perform() {
        when (val state = states.value) {
            is State.Downloading -> {
                if (state.fd != null) return
                val fd = fds.poll()
                if (fd == null) {
                    _states.value = State.Stopped
                } else {
                    _states.value = State.Downloading(fd = fd)
                }
            }
            State.Stopped -> return
        }
    }

    private fun onStartCommand(intent: Intent) {
        when (intent.action) {
            "download" -> {
                val fd = intent.getParcelableExtra<FileDelegateParcelable>("fd")
                    ?.delegate
                    ?: error("No file delegate!")
                logger.debug("download: $fd")
                fds.add(fd)
                perform()
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
    }
}
