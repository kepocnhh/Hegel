package org.kepocnhh.hegel.module.transmitter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.showToast

@Composable
internal fun TransmitterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val logics = App.logics<TransmitterLogics>()
    LaunchedEffect(Unit) {
        logics.broadcast.collect { broadcast ->
            when (broadcast) {
                is TransmitterLogics.Broadcast.OnSync -> {
                    broadcast.result.fold(
                        onSuccess = {
                            context.showToast("sync success") // todo
                        },
                        onFailure = { error ->
                            context.showToast("sync error: $error") // todo
                        },
                    )
                }
                is TransmitterLogics.Broadcast.OnAddressError -> {
                    context.showToast("address error: ${broadcast.error}") // todo
                }
            }
        }
    }
    TransmitterScreen(
        onBack = onBack,
        state = logics.state.collectAsState().value,
    )
}
