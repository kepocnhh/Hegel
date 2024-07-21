package org.kepocnhh.hegel.module.router

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import org.kepocnhh.hegel.module.bar.BarScreen
import org.kepocnhh.hegel.module.foo.FooScreen
import org.kepocnhh.hegel.module.main.MainScreen
import org.kepocnhh.hegel.module.receiver.ReceiverScreen
import org.kepocnhh.hegel.module.receiver.ReceiverService
import org.kepocnhh.hegel.module.transmitter.TransmitterScreen
import org.kepocnhh.hegel.util.compose.BackHandler
import org.kepocnhh.hegel.util.http.HttpService
import sp.kx.http.HttpReceiver
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun RouterScreen(onBack: () -> Unit) {
    BackHandler(block = onBack)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = remember {
            val value = when (ReceiverService.states.value) {
                is HttpReceiver.State.Stopped -> null
                else -> MainScreen.State.Receiver
            }
            mutableStateOf<MainScreen.State?>(value)
        }
        MainScreen(
            onState = {
                state.value = it
            },
        )
//        val duration = 250.milliseconds
        val duration = 500.milliseconds
//        val duration = 1_000.milliseconds
//        val duration = 2_000.milliseconds
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = state.value != null,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = duration.inWholeMilliseconds.toInt(),
                    easing = FastOutSlowInEasing,
                ),
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = duration.inWholeMilliseconds.toInt(),
                    easing = FastOutSlowInEasing,
                ),
            ),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
            )
        }
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = state.value != null,
            enter = slideIn(
                animationSpec = tween(
                    durationMillis = duration.inWholeMilliseconds.toInt(),
                    easing = FastOutSlowInEasing,
                ),
                initialOffset = { IntOffset(x = it.width, y = 0) },
            ),
            exit = slideOut(
                animationSpec = tween(
                    durationMillis = duration.inWholeMilliseconds.toInt(),
                    easing = FastOutSlowInEasing,
                ),
                targetOffset = { IntOffset(x = it.width, y = 0) },
            ),
        ) {
            when (remember { mutableStateOf(state.value) }.value!!) {
                MainScreen.State.Foo -> FooScreen(
                    onBack = {
                        state.value = null
                    },
                )
                MainScreen.State.Bar -> BarScreen(
                    onBack = {
                        state.value = null
                    },
                )
                MainScreen.State.Transmitter -> TransmitterScreen(
                    onBack = {
                        state.value = null
                    },
                )
                MainScreen.State.Receiver -> ReceiverScreen(
                    onBack = {
                        state.value = null
                    },
                )
            }
        }
    }
}
