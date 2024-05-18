package org.kepocnhh.hegel.module.receiver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
internal fun ReceiverScreen(onBack: () -> Unit) {
    val state = ReceiverService.state.collectAsState().value
    ReceiverScreen(
        onBack = onBack,
        state = state,
    )
}
