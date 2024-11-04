package org.kepocnhh.hegel.module.pics

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.module.files.FilesService
import org.kepocnhh.hegel.provider.BytesLoader
import kotlin.math.absoluteValue

@Composable
internal fun PicsScreen(onBack: () -> Unit) {
    val logics = App.logics<PicsLogics>()
    val state = logics.state.collectAsState().value
    val items = logics.items.collectAsState().value
    LaunchedEffect(Unit) {
        if (items == null) logics.requestItems()
    }
    if (items == null) return
    LaunchedEffect(Unit) {
        FilesService.events.collect { event ->
            when (event) {
                is BytesLoader.Event.OnLoad<String> -> {
                    logics.requestItems()
                }
                is BytesLoader.Event.OnError -> {
                    // todo
                }
            }
        }
    }
    val context: Context = LocalContext.current
    DisposableEffect(Unit) {
        onDispose {
            FilesService.stop(context = context)
        }
    }
    PicsScreen(
        onBack = onBack,
        state = state,
        items = items,
        onDelete = logics::deleteItem,
        onAdd = {
            val pointer = System.currentTimeMillis().toInt().absoluteValue % 1024
            logics.addItem("pic: $pointer")
        },
        onSetFile = { id, bytes ->
            logics.setFile(id = id, bytes = bytes)
        },
        onDetachFile = { id ->
            logics.detachFile(id = id)
        },
        onDeleteFile = { id ->
            logics.deleteFile(id = id)
        },
    )
}
