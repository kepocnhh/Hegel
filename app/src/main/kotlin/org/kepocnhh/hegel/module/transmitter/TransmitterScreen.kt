package org.kepocnhh.hegel.module.transmitter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.BackHandler
import org.kepocnhh.hegel.util.showToast

@Composable
internal fun TransmitterScreen(onBack: () -> Unit) {
    BackHandler(block = onBack)
    val context = LocalContext.current
    val logics = App.logics<TransmitterLogics>()
    val state = logics.state.collectAsState().value
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
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Box(
            modifier = Modifier
                .padding(App.Theme.insets)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                BasicText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = !state.loading) {
                            logics.syncItems()
                        }
                        .wrapContentSize(),
                    text = "sync",
                )
            }
        }
    }
}
