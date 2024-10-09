package org.kepocnhh.hegel.module.baz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.kepocnhh.hegel.App

@Composable
internal fun BazScreen(onBack: () -> Unit) {
    val logics = App.logics<BazLogics>()
    val state = logics.state.collectAsState().value
    val items = logics.items.collectAsState().value
    LaunchedEffect(Unit) {
        if (items == null) {
            logics.requestItems()
        }
    }
    if (items == null) return
    BazScreen(
        onBack = onBack,
        state = state,
        items = items,
        onDelete = logics::deleteItem,
    )
}
