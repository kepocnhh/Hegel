package org.kepocnhh.hegel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import org.kepocnhh.hegel.module.bar.BarScreen
import org.kepocnhh.hegel.module.foo.FooScreen
import org.kepocnhh.hegel.module.receiver.ReceiverScreen
import org.kepocnhh.hegel.module.receiver.ReceiverService
import org.kepocnhh.hegel.module.router.RouterScreen
import org.kepocnhh.hegel.module.transmitter.TransmitterScreen
import org.kepocnhh.hegel.util.compose.BackHandler
import org.kepocnhh.hegel.util.http.HttpService

internal class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = ComposeView(this)
        setContentView(view)
        view.setContent {
            App.Theme.Composition(
                onBackPressedDispatcher = onBackPressedDispatcher,
            ) {
                BackHandler {
                    finish()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                ) {
                    val state = remember {
                        val value = if (ReceiverService.state.value is HttpService.State.Stopped) {
                            null
                        } else {
                            RouterScreen.State.Receiver
                        }
                        mutableStateOf<RouterScreen.State?>(value)
                    }
                    when (state.value) {
                        null -> RouterScreen(
                            onState = {
                                state.value = it
                            },
                        )
                        RouterScreen.State.Foo -> FooScreen(
                            onBack = {
                                state.value = null
                            },
                        )
                        RouterScreen.State.Bar -> BarScreen(
                            onBack = {
                                state.value = null
                            },
                        )
                        RouterScreen.State.Receiver -> ReceiverScreen(
                            onBack = {
                                state.value = null
                            },
                        )
                        RouterScreen.State.Transmitter -> TransmitterScreen(
                            onBack = {
                                state.value = null
                            },
                        )
                    }
                }
            }
        }
    }
}
