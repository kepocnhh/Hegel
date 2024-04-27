package org.kepocnhh.hegel.module.router

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

internal object RouterScreen {
    enum class State {
        Foo,
        Bar,
        Receiver,
        Transmitter,
    }
}

@Composable
private fun Button(
    text: String,
    onClick: () -> Unit,
) {
    BasicText(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick)
            .wrapContentSize(),
        text = text,
        style = TextStyle(color = Color.Black)
    )
}

@Composable
internal fun RouterScreen(onState: (RouterScreen.State) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) {
            RouterScreen.State.entries.forEach { state ->
                Button(
                    text = state.name,
                    onClick = {
                        onState(state)
                    },
                )
            }
        }
    }
}
