package org.kepocnhh.hegel.module.enter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.showToast
import java.security.PrivateKey

@Composable
internal fun EnterScreen(
    onEnter: (PrivateKey) -> Unit,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val logger = remember { App.injection.loggers.create("[Enter]") }
    val logics = App.logics<EnterLogics>()
    LaunchedEffect(Unit) {
        logics.events.collect { event ->
            when (event) {
                is EnterLogics.Event.OnEnter -> event.result.fold(
                    onSuccess = onEnter,
                    onFailure = { error ->
                        logger.warning("enter error: $error")
                        context.showToast("enter error: $error")
                    },
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = logics.states.collectAsState().value
        val pinState = remember { mutableStateOf("0202") } // todo
        EnterScreen(
            state = state,
            pinState = pinState,
            onEnter = {
                val pin = pinState.value
                pinState.value = ""
                logics.enter(pin = pin)
            },
            onExit = onExit,
        )
    }
}
