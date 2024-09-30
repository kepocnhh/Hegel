package org.kepocnhh.hegel.module.pics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.kepocnhh.hegel.App
import kotlin.math.absoluteValue

@Composable
internal fun PicsScreen(onBack: () -> Unit) {
    val logics = App.logics<PicsLogics>()
    val state = logics.state.collectAsState().value
    val items = logics.items.collectAsState().value
    LaunchedEffect(Unit) {
        if (items == null) {
            logics.requestItems()
        }
    }
    if (items == null) return
    PicsScreen(
        onBack = onBack,
        state = state,
        items = items,
        onDelete = logics::deleteItem,
        onAdd = {
            val pointer = System.currentTimeMillis().toInt().absoluteValue % 1024
            logics.addItem("pic: $pointer")
        },
    )
}
