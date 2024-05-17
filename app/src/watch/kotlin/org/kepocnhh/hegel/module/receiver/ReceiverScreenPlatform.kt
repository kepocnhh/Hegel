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
import org.kepocnhh.hegel.util.http.HttpService

@Composable
internal fun ReceiverScreen(
    onBack: () -> Unit,
    state: HttpService.State,
) {
    val context = LocalContext.current
    STDBox(
        modifier = Modifier.fillMaxSize(),
        userSwipeEnabled = when (state) {
            is HttpService.State.Started -> true
            is HttpService.State.Stopped -> true
            else -> false
        },
        onDismissed = {
            when (ReceiverService.state.value) {
                is HttpService.State.Started -> {
                    HttpService.startService<ReceiverService>(
                        context,
                        HttpService.Action.StopServer,
                    )
                    onBack()
                }
                is HttpService.State.Stopped -> {
                    onBack()
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
            when (state) {
                is HttpService.State.Started -> {
                    BasicText(
                        modifier = Modifier
                            .align(Alignment.Center),
                        text = state.address,
                    )
                }
                else -> {
                    // noop
                }
            }
            val text = when (state) {
                is HttpService.State.Started -> "stop"
                is HttpService.State.Stopped -> "start"
                else -> ""
            }
            val enabled = when (state) {
                is HttpService.State.Started -> true
                is HttpService.State.Stopped -> true
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
                            is HttpService.State.Started -> {
                                HttpService.startService<ReceiverService>(
                                    context,
                                    HttpService.Action.StopServer,
                                )
                            }

                            is HttpService.State.Stopped -> {
                                HttpService.startService<ReceiverService>(
                                    context,
                                    HttpService.Action.StartServer,
                                )
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
