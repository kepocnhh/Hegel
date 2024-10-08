package org.kepocnhh.hegel.module.receiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.util.compose.STDBox
import sp.kx.http.HttpReceiver

@Composable
internal fun ReceiverScreen(
    onBack: () -> Unit,
    state: HttpReceiver.State,
) {
    val context = LocalContext.current
    STDBox(
        modifier = Modifier.fillMaxSize(),
        userSwipeEnabled = when (state) {
            is HttpReceiver.State.Started -> false
            is HttpReceiver.State.Stopped -> !state.starting
            else -> false
        },
        onDismissed = {
            when (val _state = ReceiverService.states.value) {
                is HttpReceiver.State.Started -> {
                    if (!_state.stopping) {
                        ReceiverService.startService(context, ReceiverService.Action.Stop)
                    }
                    onBack()
                }
                is HttpReceiver.State.Stopped -> {
                    if (!_state.starting) {
                        onBack()
                    }
                }
                else -> {
                    // noop
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            if (state is HttpReceiver.State.Started) {
                BasicText(
                    modifier = Modifier
                        .align(Alignment.Center),
                    text = "${state.host}:${state.port}",
                )
            }
            when (state) {
                is HttpReceiver.State.Started -> {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = "${state.host}:${state.port}",
                    )
                }
                else -> {
                    // noop
                }
            }
            val text = when (state) {
                is HttpReceiver.State.Started -> "stop"
                is HttpReceiver.State.Stopped -> "start"
                else -> ""
            }
            val enabled = when (state) {
                is HttpReceiver.State.Started -> !state.stopping
                is HttpReceiver.State.Stopped -> !state.starting
                else -> false
            }
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = enabled) {
                        when (state) {
                            is HttpReceiver.State.Started -> {
                                if (!state.stopping) {
                                    ReceiverService.startService(context, ReceiverService.Action.Stop)
                                }
                            }
                            is HttpReceiver.State.Stopped -> {
                                if (!state.starting) {
                                    ReceiverService.startService(context, ReceiverService.Action.Start)
                                }
                            }
                            else -> {
                                // noop
                            }
                        }
                    }
                    .wrapContentSize(),
                text = text,
                style = TextStyle(color = Color.White),
            )
        }
    }
}
