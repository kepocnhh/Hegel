package org.kepocnhh.hegel.module.receiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.kepocnhh.hegel.App
import org.kepocnhh.hegel.util.compose.BackHandler
import org.kepocnhh.hegel.util.http.HttpService

@Composable
internal fun ReceiverScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val state = ReceiverService.state.collectAsState().value
        Box(
            modifier = Modifier
                .padding(App.Theme.insets)
                .fillMaxSize(),
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
            val context = LocalContext.current
            BasicText(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(64.dp)
                    .clickable {
                        if (state is HttpService.State.Started) {
                            HttpService.startService<ReceiverService>(context, HttpService.Action.StopServer)
                        }
                    }
                    .wrapContentSize(),
                text = "stop",
            )
        }
    }
}
