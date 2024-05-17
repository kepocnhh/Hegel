package org.kepocnhh.hegel.module.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.kepocnhh.hegel.App
import java.util.UUID
import kotlin.math.absoluteValue

@Composable
internal fun BarScreen(onBack: () -> Unit) {
    val logics = App.logics<BarLogics>()
    val state = logics.state.collectAsState().value
    val items = logics.items.collectAsState().value
    LaunchedEffect(Unit) {
        if (items == null) {
            logics.requestItems()
        }
    }
    if (items == null) return
    BarScreen(
        onBack = onBack,
        state = state,
        items = items,
        onDelete = logics::deleteItem,
        onAdd = {
            val count = System.currentTimeMillis().toInt().absoluteValue % 1024
            logics.addItem(count = count)
        },
        onUpdate = { id: UUID ->
            val count = System.currentTimeMillis().toInt().absoluteValue % 1024
            logics.updateItem(id = id, count = count)
        },
    )
}
