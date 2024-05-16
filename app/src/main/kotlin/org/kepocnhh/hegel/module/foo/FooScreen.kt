package org.kepocnhh.hegel.module.foo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.BackHandler
import java.util.UUID

@Composable
internal fun FooScreen(onBack: () -> Unit) {
    BackHandler(block = onBack)
    val logics = App.logics<FooLogics>()
    val state = logics.state.collectAsState().value
    val items = logics.items.collectAsState().value
    LaunchedEffect(Unit) {
        if (items == null) {
            logics.requestItems()
        }
    }
    if (items == null) return
    FooScreen(
        state = state,
        items = items,
        onDelete = logics::deleteItem,
        onAdd = {
            logics.addItem(text = "foo:" + System.currentTimeMillis() % 256 + ":text")
        },
        onUpdate = { id: UUID ->
            logics.updateItem(id = id, text = "foo:" + System.currentTimeMillis() % 256 + ":updated")
        },
    )
}
