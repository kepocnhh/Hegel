package org.kepocnhh.hegel.module.receiver

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import sp.kx.http.HttpReceiver

@Composable
internal fun ReceiverScreen(
    onBack: () -> Unit,
    state: HttpReceiver.State,
) {
    val context = LocalContext.current
    BackHandler {
        when (val _state = ReceiverService.states.value) {
            is HttpReceiver.State.Started -> {
                if (!_state.stopping) {
                    ReceiverService.startService(context, ReceiverService.Action.Stop)
                }
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
    }
    val insets = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {
                        val _state = ReceiverService.states.value
                        if (_state is HttpReceiver.State.Started && !_state.stopping) {
//                            ReceiverService.startService(context, ReceiverService.Action.Stop) // todo
                        }
                    }
                    else -> {
                        // noop
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        Box(
            modifier = Modifier
                .padding(insets)
                .fillMaxSize(),
        ) {
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
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(64.dp)
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
            )
        }
    }
}
