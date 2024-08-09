package org.kepocnhh.hegel.module.router

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
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.module.auth.AuthScreen
import org.kepocnhh.hegel.module.bar.BarScreen
import org.kepocnhh.hegel.module.enter.EnterScreen
import org.kepocnhh.hegel.module.foo.FooScreen
import org.kepocnhh.hegel.module.main.MainScreen
import org.kepocnhh.hegel.module.receiver.ReceiverScreen
import org.kepocnhh.hegel.module.transmitter.TransmitterScreen

@Composable
internal fun RouterScreen(onLock: () -> Unit) {
    val state = remember { mutableStateOf<MainScreen.State?>(null) }
    when (state.value) {
        MainScreen.State.Foo -> FooScreen(onBack = { state.value = null })
        MainScreen.State.Bar -> BarScreen(onBack = { state.value = null })
        MainScreen.State.Receiver -> ReceiverScreen(onBack = { state.value = null })
        MainScreen.State.Transmitter -> TransmitterScreen(onBack = { state.value = null })
        null -> MainScreen(
            onState = {
                state.value = it
            },
            onLock = onLock,
        )
    }
}

@Composable
internal fun RouterScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val logics = App.logics<RouterLogics>()
        val state = logics.states.collectAsState().value
        LaunchedEffect(state) {
            if (state == null) logics.requestState()
        }
        if (state != null) when (state) {
            is RouterLogics.State.Keys -> if (state.authorized) {
                RouterScreen(onLock = logics::lock)
            } else {
                EnterScreen(
                    onEnter = logics::enter,
                    onExit = logics::exit,
                )
            }
            RouterLogics.State.NoKeys -> AuthScreen(onAuth = logics::auth)
        }
    }
}
