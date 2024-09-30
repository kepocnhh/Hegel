package org.kepocnhh.hegel.module.pics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import org.kepocnhh.hegel.App

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
    )
}
