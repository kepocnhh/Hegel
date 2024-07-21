package org.kepocnhh.hegel.module.receiver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import org.kepocnhh.hegel.App

@Composable
internal fun ReceiverScreen(onBack: () -> Unit) {
    val logger = remember { App.injection.loggers.create("[Receiver|Screen]") }
    val state = ReceiverService.states.collectAsState().value
    LaunchedEffect(state) {
        logger.debug("on state: $state")
    }
    ReceiverScreen(
        onBack = onBack,
        state = state,
    )
}
